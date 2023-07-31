const axios = require('axios').default;




// This finds the global ip of the device
function getIp(
    onFinished,
    onFailure
) {
    axios.get('https://api.ipify.org')
    .then( (res) => {
        console.log(res.data);
        onFinished(res.data);
    })
    .catch( (err) => {
        console.log(err);
        onFailure(err);
    })
}

module.exports = getIp;