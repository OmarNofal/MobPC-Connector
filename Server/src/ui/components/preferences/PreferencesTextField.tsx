import { Stack, Typography, FormControl, InputLabel, Select, SxProps, TextField } from '@mui/material'
import { MenuItem } from 'electron'

type PreferencesTextFieldProps = {
    sx?: SxProps
    title: string
    subtitle: string
    value: string
    type: string
    onValueChange: (_: string) => void
}

export default function PreferencesTextField(props: PreferencesTextFieldProps) {
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

            <TextField
                value={props.value}
                type={props.type}
                onChange={(evt) => props.onValueChange(evt.target.value)}
            />
        </Stack>
    )
}
