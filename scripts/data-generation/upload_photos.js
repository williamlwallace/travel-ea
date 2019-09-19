const FormData = require('form-data');
const https = require('https');
const fetch = require('node-fetch');

const ADMIN_COOKIE = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw';


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
const photos = require(`./${commandArgs[1]}`); // read json
createReqs();


async function createReqs() {
    let promises = [];
    for (key of Object.keys(photos)) {
        //create list of promises and limit too a certain size to not overload server
        promises = promises.concat(photos[key].map(sendPhoto.bind(null, key, postReq)));
        //Limit number to send to 50
        if (promises.length >= 50) {
            await Promise.all(promises);
            promises = [];
        }
        
    }
}



function sendPhoto(photo, postReq,  userId) {
    return new Promise((resolve, reject) => {
        const form = new FormData();
        console.log('ya');
        https.get(photo, (res) => {
            form.append('file', res, photo);
            form.append('profilePhotoName', "profilepic.jpg");
            form.append('caption', '');
            form.append('userUploadId', userId);
            form.append('tags', "[]");
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
        return res.json()
        .then(json => {
            if (res.status === 201) {
                console.log("sucess 1")
                return setProfilePicture(postReq, json.userId, json.guid);
            } else {
                console.log('\n\n######Error######');
                console.log("Failed to upload photo")
                console.log(res.statusMessage);
                console.log(res.status);
                console.log(json);
                console.log('\n');
                return;
            }
        });
    });
}

function setProfilePicture(postReq, userId, photoId) {
    return fetch(`http://${postReq.host}:${postReq.port}${postReq.path}/${userId}/profile`, {
        method: 'put',
        headers: postReq.headers,
        body: photoId
    })
    .then(res => {
        if (res.status !== 200) {
            console.log('\n\n######Error######');
            console.log("Failed to set to profile picture")
            console.log(res.statusMessage);
            console.log(res.status);
            // res.json().then(json => console.log(json));
            console.log('\n');
        } else {
            console.log("success 2");
        }
    });
}