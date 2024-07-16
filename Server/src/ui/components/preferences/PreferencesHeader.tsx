import { Typography, useTheme } from '@mui/material'

type PreferencesHeaderProps = {
    title: string
}

export default function PreferencesHeader({ title }: PreferencesHeaderProps) {
    const palette = useTheme().palette

    return (
        <Typography
            variant='body1'
            fontWeight={'400'}
            color={palette.primary.main}
            sx={{ userSelect: 'none' }}
        >
            {title}
        </Typography>
    )
}
