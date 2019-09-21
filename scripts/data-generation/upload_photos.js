const FormData = require('form-data');
const https = require('https');
const fetch = require('node-fetch');

const ADMIN_COOKIE = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw';

let successCounter = 0;
let failureCounter = 0;

const commandArgs = process.argv.slice(2);

if (commandArgs.length !== 2) {
    console.log('usage: upload_photos.js port photosFileName.json');
    return;
}

const port = commandArgs[0];
const postReq = {
    host: 'localhost',
    port,
    path: '/api/photo',
    headers: {'Cookie': 'JWT-Auth='+ADMIN_COOKIE}
}
console.log("Loading data...");
const photos = require(`./${commandArgs[1]}`); // read json
console.log("Data loaded\n");
createReqs();


async function createReqs() {
    let promises = [];
    console.log("Creating Requests...");
    for (key of Object.keys(photos)) {
        //create list of promises and limit too a certain size to not overload server
        promises = promises.concat(photos[key].map(sendPhoto.bind(null, key, postReq)));
        //Limit number to send to 50
        if (promises.length >= 100) {
            console.log("Sending Requests...");
            await Promise.all(promises);
            promises = [];
            console.log("Creating Requests...");
        }
    }
    console.log("\N#####All Photos Loaded#####");
    console.log("Profile Pictures succesfully added: " + successCounter);
    console.log("Profile Pictures failed to add: " + failureCounter);

}



function sendPhoto(photo, postReq,  userId) {
    return new Promise((resolve, reject) => {
        const form = new FormData();
        https.get(photo, (res) => {
            form.append('file', res, photo);
            form.append('profilePhotoName', "profilepic.jpg");
            form.append('caption', '');
            form.append('userUploadId', userId);
            form.append('tags', JSON.stringify([]));
            postMultipart(postReq, form).then(resolve);
        });
    });
    
}

/**
 *
 * @param url url to send request to
 * @param form The already created formdata object to upload
 */
function postMultipart(postReq, form) {
    return fetch(`http://${postReq.host}:${postReq.port}${postReq.path}`, {
        method: 'post',
        headers: postReq.headers,
        body: form
    })
    .then(res => {
        if (res.status !== 201) {
            failureCounter += 1;
            return;
        }
        return res.json()
        .then(json => {
            return setProfilePicture(postReq, json.userId, json.guid);
        });
    });
}

function setProfilePicture(postReq, userId, photoId) {
    return fetch(`http://${postReq.host}:${postReq.port}${postReq.path}/${userId}/profile`, {
        method: 'PUT',
        headers: { ...postReq.headers, 'Content-Type': 'application/json' },
        body: JSON.stringify(photoId)
    })
    .then(res => {
        if (res.status !== 200) {
            failureCounter += 1;
        } else {
            successCounter += 1;
        }
    });
}