import {
    AppBar,
    Card,
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
import { useMemo, useState } from 'react'
import DevicesScreenViewModel from './DevicesScreenViewModel'
import { useUnwrap } from '../../utils'
import DeviceList from '../../components/device/DeviceList'

export type DevicesScreenProps = {
    devices: string[]

    onAddNewDevice: () => void
}

export function DevicesScreen() {
    const viewModel = useMemo(() => new DevicesScreenViewModel(), [])
    const state = useUnwrap(viewModel.state)

    const palette = useTheme().palette

    console.log(state)
    const [open, setOpenDialog] = useState(false)

    const content =
        state.state == 'loading' ? (
            <></>
        ) : (
            <Card
                elevation={3}
                sx={{ padding: '24px' }}
            >
                <Typography
                    variant='h5'
                    fontWeight={'700'}
                >
                    Paired Devices
                </Typography>
                <Divider sx={{ width: 'auto' }} />
                <DeviceList
                    onDelete={viewModel.deleteDevice}
                    devices={state.devices}
                    sx={{ width: '100%', marginTop: '32px' }}
                />
            </Card>
        )
    return (
        <Stack
            sx={{ flex: 1 }}
            direction={'column'}
            height={'100%'}
        >
            <PairingDialog
                open={open}
                onClose={() => setOpenDialog(false)}
            />
            <TopBar
                sx={{ bgcolor: palette.background.default }}
                title='Devices'
                onOpenPairingDialog={() => setOpenDialog(true)}
            />
            <Divider variant='fullWidth' />
            <Paper
                sx={{
                    height: '100%',
                    backgroundColor: palette.background.paper,
                    borderRadius: 0,
                    padding: '32px',
                }}
                elevation={1}
            >
                {content}
            </Paper>
        </Stack>
    )
}

function TopBar(props: { title: string; sx?: SxProps<Theme>; onOpenPairingDialog: () => void }) {
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
                    <IconButton
                        size='large'
                        onClick={props.onOpenPairingDialog}
                    >
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
