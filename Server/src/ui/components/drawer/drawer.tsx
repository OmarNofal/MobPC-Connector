import {
    Divider,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    SxProps,
    Theme,
    Toolbar,
    Typography,
    useTheme,
} from '@mui/material'
import { Screen } from '../../model/screens'

import ServerIconFilled from '@mui/icons-material/Laptop'
import ServerIconOutlined from '@mui/icons-material/LaptopOutlined'
import MobileIconFilled from '@mui/icons-material/PhoneAndroid'
import MobileIconOutlined from '@mui/icons-material/PhoneAndroidOutlined'
import SettingsIconFilled from '@mui/icons-material/Settings'
import SettingsIconOutlined from '@mui/icons-material/SettingsOutlined'
import { useNavigate } from 'react-router-dom'

type AppDrawerProps = {
    screenWidthPercentage: string
    selectedScreen: Screen
}

export default function AppDrawer(props: AppDrawerProps) {
    const navigate = useNavigate()

    const navigateToScreen = (screen: Screen) => {
        switch (screen) {
            case Screen.SERVER_SCREEN:
                navigate('/')
                break
            case Screen.DEVICES_SCREEN:
                navigate('/devices')
                break
            case Screen.PREFERENCES_SCREEN:
                navigate('/preferences')
                break
        }
    }

    const selected = props.selectedScreen

    return (
        <Drawer
            variant='permanent'
            sx={{ width: props.screenWidthPercentage }}
            PaperProps={{ sx: { width: props.screenWidthPercentage } }}
        >
            <Toolbar sx={{ padding: '12px' }}>
                <Typography
                    variant='h6'
                    fontWeight={'800'}
                >
                    MobPC Connector
                </Typography>
            </Toolbar>

            <Divider />

            <List sx={{ flex: 1, flexDirection: 'column', display: 'flex' }}>
                <DrawerNavigationRow
                    onClick={() => navigateToScreen(Screen.SERVER_SCREEN)}
                    sx={{ padding: '16px', borderRadius: '12px' }}
                    title='Server'
                    outlinedIcon={ServerIconOutlined}
                    filledIcon={ServerIconFilled}
                    isSelected={selected == Screen.SERVER_SCREEN}
                />

                <Divider variant='middle' />

                <DrawerNavigationRow
                    onClick={() => navigateToScreen(Screen.DEVICES_SCREEN)}
                    sx={{ padding: '16px', borderRadius: '12px' }}
                    title='Devices'
                    outlinedIcon={MobileIconOutlined}
                    filledIcon={MobileIconFilled}
                    isSelected={selected == Screen.DEVICES_SCREEN}
                />

                <Divider variant='middle' />

                <div style={{ flex: 1 }}></div>

                <Divider />

                <DrawerNavigationRow
                    onClick={() => navigateToScreen(Screen.PREFERENCES_SCREEN)}
                    title='Preferences'
                    outlinedIcon={SettingsIconOutlined}
                    filledIcon={SettingsIconFilled}
                    isSelected={selected == Screen.PREFERENCES_SCREEN}
                />
            </List>
        </Drawer>
    )
}

type DrawerNavigationRowProps = {
    sx?: SxProps<Theme>
    title: string
    onClick: () => void
    isSelected: boolean
    outlinedIcon: any
    filledIcon: any
}

function DrawerNavigationRow(props: DrawerNavigationRowProps) {
    const colors = useTheme().palette

    return (
        <ListItem sx={{ ...props.sx, ...{ alignItems: 'flex-end' } }}>
            {/* <Link to='/devices'> */}
            <ListItemButton
                sx={{
                    padding: '16px',
                    borderRadius: '12px',
                }}
                selected={props.isSelected}
                onClick={props.onClick}
            >
                <ListItemIcon>{props.isSelected ? <props.filledIcon /> : <props.outlinedIcon />}</ListItemIcon>
                <Typography
                    variant={'h6'}
                    fontWeight={props.isSelected ? '500' : 'normal'}
                    color={props.isSelected ? colors.primary.main : colors.text.disabled}
                >
                    {props.title}
                </Typography>
            </ListItemButton>
            {/* </Link> */}
        </ListItem>
    )
}
