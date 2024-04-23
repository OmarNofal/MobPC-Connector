"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const axios = require('axios').default;
// This finds the global ip of the device
function getIp(onFinished, onFailure) {
    axios.get('https://api.ipify.org')
        .then((res) => {
        console.log(res.data);
        onFinished(res.data);
    })
        .catch((err) => {
        console.log(err);
        onFailure(err);
    });
}
exports.default = getIp;
