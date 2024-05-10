import { SxProps, Theme, Stack } from '@mui/material'
import { Device } from '../../../model/device'
import DeviceRow from './DeviceRow'

type Prop = {
    devices: Device[]
    onDelete: (deviceId: string) => void
    sx?: SxProps<Theme>
}

export default function DeviceList(props: Prop) {
    const rows = props.devices
        .sort((a, b) => -new Date(a.pairingDate).getMilliseconds() + new Date(b.pairingDate).getMilliseconds())
        .map((d) => {
            return (
                <DeviceRow
                    device={d}
                    key={d.id}
                    sx={{
                        padding: ' 16px',
                    }}
                    onDelete={() => props.onDelete(d.id)}
                ></DeviceRow>
            )
        })

    return (
        <Stack
            direction={'column'}
            sx={props.sx}
            spacing={'16px'}
        >
            {rows}
        </Stack>
    )
}
