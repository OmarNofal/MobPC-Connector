import { Stack, Typography, FormControl, InputLabel, Select, SxProps, TextField, IconButton } from '@mui/material'
import { MenuItem } from 'electron'
import InfoIcon from '@mui/icons-material/InfoOutlined'

type PreferencesTextFieldProps = {
    sx?: SxProps
    title: string
    subtitle: string
    value: string
    type: string
    infoIcon?: boolean
    onInfoIconClicked?: () => void
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

            <TextField
                value={props.value}
                type={props.type}
                onChange={(evt) => props.onValueChange(evt.target.value)}
            />
        </Stack>
    )
}
