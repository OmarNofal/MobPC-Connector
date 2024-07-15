import { FormControl, InputLabel, MenuItem, Select, Stack, SxProps, Typography } from '@mui/material'

type PreferencesMultiselectProps = {
    sx?: SxProps
    choices: Array<string>
    selectedIndex: number
    title: string
    subtitle: string
    onOptionSelected: (_: number) => void
}

export default function PreferencesMultiselect(props: PreferencesMultiselectProps) {
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
                <Typography variant='h5'>{props.title}</Typography>
                <Typography variant='caption'>{props.subtitle}</Typography>
            </Stack>

            <div style={{ flex: '1' }}></div>

            <FormControl sx={{ flex: '0.5' }}>
                <InputLabel id='demo-simple-select-label'>Theme</InputLabel>
                <Select
                    value={props.selectedIndex}
                    label='Theme'
                    onChange={(event) => props.onOptionSelected(event.target.value as number)}
                >
                    {props.choices.map((val, index) => {
                        return <MenuItem value={index}>{val}</MenuItem>
                    })}
                </Select>
            </FormControl>
        </Stack>
    )
}
