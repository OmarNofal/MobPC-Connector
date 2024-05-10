import InfoOutlined from '@mui/icons-material/InfoOutlined'
import { Button, Card, Divider, PaletteMode, Stack, SxProps, Theme, Tooltip, Typography, useTheme } from '@mui/material'
import { ServerScreenInitializedState } from '../../screens/server/ServerScreenViewModel'
import InformationRow from './informationRow'

type ServerInformationProps = {
    sx?: SxProps<Theme>
    serverState: ServerScreenInitializedState
    onToggleServer: () => void
}

export default function ServerInformationCard(props: ServerInformationProps) {
    
    const name = props.serverState.name
    const isDarkTheme = useTheme().palette.mode == 'dark'

    return (
        <Card
            sx={{ padding: '16px', ...props.sx, display: 'flex', flexDirection: 'column' }}
            elevation={6}
        >
            <Typography
                variant='h5'
                fontWeight={'medium'}
            >
                Server Status
            </Typography>

            <InformationRow
                title='Server Name'
                value={name}
                sx={{
                    width: '100%',
                    flex: 1,
                    marginTop: '32px',
                }}
            />

            <Divider variant='fullWidth' />

            <InformationRow
                title='HTTPS Port'
                value={props.serverState.httpsPort.toString()}
                sx={{
                    width: '100%',
                    marginTop: '24px',
                    flex: 1,
                }}
            />

            <Divider variant='fullWidth' />

            <InformationRow
                title='HTTP Port'
                value={props.serverState.httpPort.toString()}
                sx={{
                    width: '100%',
                    marginTop: '24px',
                }}
                info='Beacuse the HTTPS port uses a self-signed SSL certificate, some external apps (like VLC Media Player) will refuse to open any URL through this port.
                This HTTP port is used to allow you to open files on external apps on your device, such as streaming 
                movies, viewing PDF documents, etc..'
            />
            <Divider variant='fullWidth' />

            <InformationRow
                title='Server Status'
                value={props.serverState.isRunning ? 'Running' : 'Stopped'}
                sx={{
                    width: '100%',
                    marginTop: '24px',
                }}
                valueColor={getColorForServerState(props.serverState.isRunning, isDarkTheme ? 'dark' : 'light')}
            />

            <Divider variant='fullWidth' />

            <Button
                sx={{ marginTop: '16px', width: 'auto' }}
                variant={props.serverState.isRunning ? 'outlined' : 'contained'}
                onClick={props.onToggleServer}
            >
                <Typography variant='h6'>{props.serverState.isRunning ? 'Stop Server' : 'Start Server'}</Typography>
            </Button>
        </Card>
    )
}

function getColorForServerState(isRunning: boolean, theme: 'light' | 'dark') {
    if (isRunning && theme == 'light') return 'green'
    else if (isRunning && theme == 'dark') return 'lightgreen'
    else if (!isRunning && theme == 'light') return 'red'
    else return '#ff4f7c'
}
