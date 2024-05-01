import { Card, Divider, SxProps, Theme, Typography } from '@mui/material'
import { NetworkInterface } from '../../../utilities/networkInterfaces'
import InformationRow from './informationRow'

export type NetworkInterfacesCardProps = {
    sx?: SxProps<Theme>
    interfaces: NetworkInterface[]
}

export default function NetworkInterfacesCard(props: NetworkInterfacesCardProps) {
    return (
        <Card
            sx={{ padding: '16px', ...props.sx, display: 'flex', flexDirection: 'column' }}
            elevation={6}
        >
            <Typography
                variant='h5'
                fontWeight={'medium'}
            >
                Network Interfaces
            </Typography>

            {props.interfaces.map((iface, i) => {
                return (
                    <>
                        <InformationRow
                            title={iface.name}
                            value={iface.ipv4}
                            sx={{
                                width: '100%',
                                flex: 1,
                                marginTop: '32px',
                            }}
                        />
                        <Divider variant='fullWidth' />
                    </>
                )
            })}

        </Card>
    )
}
