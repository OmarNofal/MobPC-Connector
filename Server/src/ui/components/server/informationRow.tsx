import { Theme } from '@emotion/react'
import { SxProps, Stack, Typography, Tooltip } from '@mui/material'
import InfoOutlined from '@mui/icons-material/InfoOutlined'

export default function InformationRow({
    title,
    value,
    sx,
    info,
    valueColor,
}: {
    title: string
    value: string
    sx: SxProps<Theme>
    info?: string
    valueColor?: string
}) {
    return (
        <Stack
            sx={{ ...sx }}
            direction='row'
        >
            <Typography
                variant='body2'
                fontSize='1.1rem'
            >
                {title}
            </Typography>
            {info != undefined ? (
                <InfoTooltip
                    text={info}
                    sx={{ marginLeft: '12px' }}
                />
            ) : (
                <></>
            )}
            <div style={{ flex: 1 }} />
            <Typography
                fontWeight={'600'}
                variant='body1'
                fontSize='1.2rem'
                color={valueColor}
            >
                {value}
            </Typography>
        </Stack>
    )
}

function InfoTooltip({ text, sx }: { sx: SxProps<Theme>; text: string }) {
    return (
        <Tooltip
            sx={sx}
            title={<Typography fontSize={'1.1rem'}>{text}</Typography>}
        >
            <InfoOutlined />
        </Tooltip>
    )
}
