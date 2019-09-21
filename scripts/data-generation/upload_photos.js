const FormData = require('form-data');
const https = require('https');
const fetch = require('node-fetch');                              
const fs = require('fs');
const { resolve } = require('path');
const photos = require('./photo_urls_and_users.json'); // read json

const ADMIN_COOKIE = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw';
const DESTINATION_ID_MAX = 1000;
const EVOLUTION_FILE = "./conf/evolutions/default/3.sql";
const PHOTO_SIZES = [240, 270, 300, 330, 360];

const commandArgs = process.argv.slice(2);

if (commandArgs.length !== 1) {
    console.log('usage: storage/directory');
    return;
}

const storageDirectory = commandArgs[0];
const stream = fs.createWriteStream(EVOLUTION_FILE, {flags:'a'});
let photoId = 1;

console.log("\nDownloading profile pictures...");

createProfileReqs()
.then(() => {
    console.log("profile photos completed\n")
    console.log("\nDownloading Destination Photos...");
    createDestinationPhotos()
    .then(() => {
        console.log('Destination photos completed\n');
        addDowns();
        stream.end();
        console.log('Data generation complete');
    });
});

/**
 * Downloads a file from a url to given directory and name
 *
 * @param {stirng} URL address of photo
 * @param {string} filename directory and filename ot save
 */
function downloadPhotoToFile(URL, filename) {
    return new Promise((resolve, reject) => {
        https.get(URL, function(res) {
            const file = fs.createWriteStream(filename);
            res.pipe(file);
            return resolve();
        });
    });
}

/**
 * Downloads random photos for all destinations making them profile photo
 */
async function createDestinationPhotos() {
    let promises = [];
    for (let i = 1; i <= DESTINATION_ID_MAX; i++) {
        const filename = `${storageDirectory}/photos/dest_photo_${i}.jpg`;
        const width = PHOTO_SIZES[Math.floor(Math.random()*PHOTO_SIZES.length)];
        const height = PHOTO_SIZES[Math.floor(Math.random()*PHOTO_SIZES.length)];
        const URL = `https://picsum.photos/id/${i + 79}/${width}/${height}`;
        promises.push(downloadPhotoToFile(URL, filename));
        //limit to 150 at a time
        if (promises.length > 150) {
            await Promise.all(promises);
            promises = [];
        }
    }
    generateDestPictureSql();
}

/**
 * Downloads photos for given people making them profile photo
 */
async function createProfileReqs() {
    let promises = [];
    let users = [];
    for (key of Object.keys(photos)) {
        //create list of promises and limit too a certain size to not overload server
        promises = promises.concat(photos[key].map(sendPhoto.bind(null, key)));
        users = users.concat(photos[key]);

        //Limit number to send to 50
        if (promises.length >= 50) {
            await Promise.all(promises);
            promises = [];
        }
    }
    generateProfilePictureSql(users);
}


/**
 * Downloads photo for given user
 */
function sendPhoto(photo, userId) {
    const filename = `${storageDirectory}/photos/user_photo_${userId}.jpg`;
    return downloadPhotoToFile(photo, filename);
}

/**
 * Creates sql for destination primary photos
 */
function generateDestPictureSql() {
    for (let i = 1; i <= DESTINATION_ID_MAX; i++) {
        const filename = resolve(`${storageDirectory}/photos/dest_photo_${i}.jpg`);
        const insertPhoto = `INSERT INTO Photo(user_id, filename, thumbnail_filename, used_for_profile) VALUES (${i}, '${filename}', '${filename}', 0);`;
        const destinationPhoto = `INSERT INTO DestinationPhoto(photo_id, destination_id) VALUES (${photoId}, ${i});`;
        const updateProfile = `UPDATE Destination SET primary_photo_guid = ${photoId} WHERE id=${i};`
        stream.write(insertPhoto + '\n');
        stream.write(destinationPhoto + '\n');
        stream.write(updateProfile + '\n\n');
        photoId += 1
    }
}

/**
 * Creates sql for profile photos and insertes it into evolutions
 */
function generateProfilePictureSql(users) {
    for (const user of users) {
        const filename = resolve(`${storageDirectory}/photos/user_photo_${user}.jpg`);
        const insertPhoto = `INSERT INTO Photo(user_id, filename, thumbnail_filename, used_for_profile) VALUES (${user}, '${filename}', '${filename}', 1);`;
        const updateProfile = `UPDATE Profile SET profile_photo_guid = ${photoId} WHERE user_id=${user};`
        stream.write(insertPhoto + '\n');
        stream.write(updateProfile + '\n\n');
        photoId += 1;
    }
}

/**
 * Adds downs to evolutions
 */
function addDowns() {
    const downs = `\n-- !Downs
DELETE FROM Likes;
DELETE FROM NewsFeedEvent;
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM PendingDestinationPhoto;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;`
    stream.write(downs);
};