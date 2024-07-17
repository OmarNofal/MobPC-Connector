import { AppBar, Divider, SxProps, Theme, Toolbar, Typography } from '@mui/material'

type TopBarProps = {
    sx?: SxProps<Theme>
    title: string
    divider?: boolean
}

export default function TopBar(props: TopBarProps) {
    return (
        <AppBar
            position='sticky'
            sx={{ ...props.sx, boxShadow: 'none' }}
            elevation={1}
        >
            <Toolbar sx={{ padding: '12' }}>
                <Typography
                    variant='h3'
                    sx={{ userSelect: 'none' }}
                    color={(theme) => theme.palette.primary.light}
                    fontWeight={'600'}
                >
                    {props.title}
                </Typography>
            </Toolbar>
            {props.divider && <Divider variant='fullWidth' />}
        </AppBar>
    )
}
