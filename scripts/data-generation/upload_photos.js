const FormData = require('form-data');
const https = require('https');

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
    headers: {'JWT-Auth': ADMIN_COOKIE}
}
const photos = require(`./${commandArgs[1]}`); // read json

for (key of Object.keys(photos)) {
    photos[key].map(sendPhoto.bind(null, key, postReq));
}


function sendPhoto(photo, postReq,  userId) {
    const form = new FormData();
    console.log('ya');
    https.get(photo, (res) => {

        
        form.append('file', res, photo);
        form.append('caption', '');
        form.append('userUploadId', userId);
        form.append('tags', "[]");
        postMultipart(postReq, form);
    });
}

/**
 *
 * @param url url to send request to
 * @param form The already created formdata object to upload
 */
function postMultipart(postReq, form) {
    form.submit(postReq, (err, res) => {
        console.log('yeeet');
        
        if (err) {
            console.log(err);
            return;
        }
        console.log(res);
        res.resume();
    });
}