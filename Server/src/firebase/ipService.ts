const axios = require('axios').default;




// This finds the global ip of the device
export default function getIp(
    onFinished: (ip: string) => void,
    onFailure: (err: any) => void
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
