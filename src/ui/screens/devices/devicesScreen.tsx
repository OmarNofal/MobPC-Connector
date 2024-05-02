import {
    AppBar,
    Divider,
    IconButton,
    Paper,
    Stack,
    SxProps,
    Theme,
    Toolbar,
    Tooltip,
    Typography,
    useTheme,
} from '@mui/material'
import Devices from '@mui/icons-material/Devices'
import PairingDialog from '../../components/pairing/pairingDialog'

export type DevicesScreenProps = {
    devices: string[]

    onAddNewDevice: () => void
}

export function DevicesScreen(props: DevicesScreenProps) {
    const palette = useTheme().palette

    return (
        <Stack
            sx={{ flex: 1 }}
            direction={'column'}
            height={'100%'}
        >
            <PairingDialog />
            <TopBar
                sx={{ bgcolor: palette.background.default }}
                title='Devices'
            />
            <Divider variant='fullWidth' />
            <Paper
                sx={{
                    width: '100%',
                    height: '100%',
                    backgroundColor: palette.background.paper,
                    borderRadius: 0,
                }}
                elevation={1}
            ></Paper>
        </Stack>
    )
}

function TopBar(props: { title: string; sx?: SxProps<Theme> }) {
    return (
        <AppBar
            position='sticky'
            sx={{ ...props.sx, boxShadow: 'none' }}
            elevation={1}
        >
            <Toolbar sx={{ padding: '12' }}>
                <Typography
                    variant='h3'
                    component={'div'}
                    sx={{ userSelect: 'none', flexGrow: 1 }}
                    color={(theme) => theme.palette.primary.light}
                    fontWeight={'600'}
                >
                    {props.title}
                </Typography>
                <Tooltip title={<Typography variant='body1'>Pair a new device</Typography>}>
                    <IconButton size='large'>
                        <Devices
                            fontSize='large'
                            sx={{
                                color: (theme) => theme.palette.text.secondary,
                            }}
                        />
                    </IconButton>
                </Tooltip>
            </Toolbar>
        </AppBar>
    )
}
