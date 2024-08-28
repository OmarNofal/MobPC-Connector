import {
    FormControl,
    IconButton,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    Switch,
    SxProps,
    ToggleButton,
    Typography,
} from '@mui/material'

import InfoIcon from '@mui/icons-material/InfoOutlined'

export type ToggleablePreference = {
    isToggled: boolean
    sx?: SxProps
    title: string
    subtitle: string
    infoIcon?: boolean
    onInfoIconClicked?: () => void
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
                <Stack
                    display='flex'
                    flexDirection={'row'}
                    alignItems={'center'}
                >
                    <Typography
                        variant='h6'
                        sx={{ userSelect: 'none' }}
                    >
                        {props.title}
                    </Typography>
                    {props.infoIcon ? (
                        <IconButton
                            sx={{ marginLeft: '6px' }}
                            disabled={false}
                            onClick={props.onInfoIconClicked}
                        >
                            <InfoIcon />
                        </IconButton>
                    ) : (
                        <></>
                    )}
                </Stack>

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
