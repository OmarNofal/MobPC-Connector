import Devices from '@mui/icons-material/Devices'
import {
    Button,
    Collapse,
    Dialog,
    DialogActions,
    DialogTitle,
    Divider,
    IconButton,
    Stack,
    Typography,
    darken,
    lighten,
    useTheme,
} from '@mui/material'

import { useMemo } from 'react'
import PairingViewModel from './pairingDialogViewModel'
import { useUnwrap } from '../../utils'
import CloseOutlined from '@mui/icons-material/CloseOutlined'

export default function PairingDialog() {
    const viewModel = useMemo(() => new PairingViewModel(), [])

    const state = useUnwrap(viewModel.state)

    const qrImageBase64 = state.state == 'loading' ? 'none' : state.qrBase64

    return (
        <Dialog
            open={true}
            PaperProps={{
                sx: {
                    borderTop: '5px solid',
                    borderColor: titleBorderColor(),
                },
            }}
            fullWidth
        >
            <div>
                <DialogTitle
                    variant='h4'
                    fontWeight={500}
                >
                    <div style={{ flexDirection: 'row', display: 'flex', gap: '18px', alignItems: 'center' }}>
                        <Devices
                            fontSize='medium'
                            sx={{
                                opacity: '60%',
                            }}
                        />
                        <Typography
                            variant='h5'
                            style={{ userSelect: 'none' }}
                        >
                            Pair a New Device
                        </Typography>
                        <div style={{ flex: '1', display: 'flex', justifyContent: 'flex-end' }}>
                            <IconButton>
                                <CloseOutlined sx={{ opacity: '60%' }} />
                            </IconButton>
                        </div>
                    </div>
                </DialogTitle>
                <Divider variant='fullWidth' />
            </div>
            <Stack
                direction={'column'}
                paddingTop={'18px'}
            >
                <Typography
                    fontWeight={500}
                    fontSize={18}
                    marginBottom={'6px'}
                    paddingLeft={'24px'}
                >
                    Open the Android app, and scan this QR code to pair.
                </Typography>
                <Typography
                    fontWeight={400}
                    variant='subtitle2'
                    marginBottom={'24'}
                    paddingLeft={'24px'}
                    fontStyle={'italic'}
                    sx={{
                        textDecoration: 'underline',
                    }}
                >
                    Never share this code with anyone
                </Typography>
                <Collapse in={qrImageBase64 != 'none'}>
                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        <img
                            src={qrImageBase64}
                            style={{ maxWidth: '80%' }}
                        />
                    </div>
                </Collapse>
            </Stack>
            <DialogActions
                sx={{
                    justifyContent: 'space-between',
                    background: dialogActionsColors().background,
                }}
            >
                <Button sx={{ color: dialogActionsColors().textColor }}>Cancel</Button>
            </DialogActions>
        </Dialog>
    )
}

function dialogActionsColors() {
    const palette = useTheme().palette

    return {
        background:
            palette.mode == 'dark' ? lighten(palette.background.default, 0.2) : darken(palette.background.default, 0.1),
        textColor: palette.mode == 'dark' ? '#BBBBBB' : '#888888',
    }
}

function titleBorderColor() {
    const palette = useTheme().palette
    return palette.mode == 'dark' ? palette.primary.dark : palette.primary.light
}
