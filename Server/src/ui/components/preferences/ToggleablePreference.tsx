import {
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    Switch,
    SxProps,
    ToggleButton,
    Typography,
} from '@mui/material'

export type ToggleablePreference = {
    isToggled: boolean
    sx?: SxProps
    title: string
    subtitle: string
    toggle: () => void
}

export function ToggleablePreference(props: ToggleablePreference) {
    return (
        <Stack
            display={'flex'}
            flexDirection={'row'}
            alignItems={'center'}
            sx={props.sx}
        >
            <Stack
                display={'flex'}
                flexDirection={'column'}
            >
                <Typography
                    variant='h6'
                    sx={{ userSelect: 'none' }}
                >
                    {props.title}
                </Typography>
                <Typography
                    variant='caption'
                    sx={{ userSelect: 'none' }}
                >
                    {props.subtitle}
                </Typography>
            </Stack>

            <div style={{ flex: '1' }}></div>

            <Switch
                checked={props.isToggled}
                onClick={props.toggle}
                size='medium'
                sx={{ transform: 'scale(1.3)' }}
            />
        </Stack>
    )
}
