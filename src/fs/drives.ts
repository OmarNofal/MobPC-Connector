import drivelist from 'drivelist';



export default function getDrives(
    onError,
    onSuccess
) {

    drivelist.list( (err, devices) => {
        if (err) onError(err);
        else onSuccess(devices);
    });

}
