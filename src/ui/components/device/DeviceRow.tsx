import { Card, IconButton, SxProps, Theme, Typography, useTheme } from '@mui/material'
import { Device } from '../../../model/device'
import { Stack } from '@mui/system'
import Smartphone from '@mui/icons-material/AndroidTwoTone'
import Trash from '@mui/icons-material/DeleteForever'

type Props = {
    device: Device
    onDelete: () => void
    sx?: SxProps<Theme>
}

export default function DeviceRow(props: Props) {
    const device = props.device
    const date = new Date(device.pairingDate).toLocaleDateString()
    return (
        <Card
            sx={props.sx}
            elevation={6}
        >
            <Stack
                direction={'row'}
                alignItems={'center'}
                display={'flex'}
                flexDirection={'row'}
            >
                <Smartphone sx={{ fontSize: '48px', opacity: '0.8', color: '#3DDC84' }} />
                <Stack
                    direction={'column'}
                    marginLeft={'16px'}
                >
                    <Typography
                        variant='h6'
                        fontWeight={'600'}
                    >
                        {device.modelName}
                    </Typography>
                    <Typography
                        variant='subtitle2'
                        fontWeight={'300'}
                    >
                        {device.os + '   •   Paired since ' + date}
                    </Typography>
                </Stack>
                <div style={{ flex: 1, display: 'flex', justifyContent: 'flex-end' }}>
                    <IconButton onClick={props.onDelete}>
                        <Trash
                            htmlColor={useTheme().palette.error.light}
                            sx={{ opacity: 0.8, fontSize: '28px' }}
                        />
                    </IconButton>
                </div>
            </Stack>
        </Card>
    )
}
