import drivelist from 'drivelist';



export default function getDrives(
    onError: (error: any) => void,
    onSuccess: (drives: drivelist.Drive[]) => void 
) {

    drivelist.list( (err, devices) => {
        if (err) onError(err);
        else onSuccess(devices);
    });

}
