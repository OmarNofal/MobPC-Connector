import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'

const info = `
    In order to be able to access your server from anywhere in the world, and not just in your home's network,\ 
    your router must be able to forward requests from the outside global network \
    to your local network.

    To be able to do this, your router has to know three things:
        1. Your server's local IP address inside your home network
        2. Your server's port
        3. The port which will be accessed from the outside world by your Phone

    Here are the steps to give it access to this information:
        1. Give your PC a static IP address in your local network that doesn't change on every restart
        (Follow this: https://www.trendnet.com/press/resource-library/how-to-set-static-ip-address)
        
        2. Open your router settings and configure port forwarding to forward requests\
        from the 'Global Port' to your chosen static IP address and 'Server Port'

        This step depends on your router. Search Google on how to \
        configure port forwarding for your specfic router model
        
        3. Enjoy connectivity to your server from anywhere
        `

export type InformationDialogProps = {
    open: boolean
    closeDialog: () => void
}

export default function GlobalPortInformationDialogs(props: InformationDialogProps) {
    return (
        <Dialog
            open={props.open}
            onClose={props.closeDialog}
            scroll='paper'
            fullWidth
            maxWidth={'sm'}
        >
            <DialogTitle>Global Connectivity</DialogTitle>
            <DialogContent>
                <DialogContentText sx={{ whiteSpace: 'pre-line' }}>{info}</DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button
                    variant='text'
                    onClick={props.closeDialog}
                >
                    Understood
                </Button>
            </DialogActions>
        </Dialog>
    )
}
