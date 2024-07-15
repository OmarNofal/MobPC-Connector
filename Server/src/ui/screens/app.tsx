import { Stack, ThemeProvider, Typography, createTheme, useMediaQuery } from '@mui/material'
import { useEffect, useMemo } from 'react'
import { Route, Routes, useLocation } from 'react-router-dom'
import AppDrawer from '../components/drawer/drawer'
import { Screen } from '../model/screens'
import ServerScreen from './server/ServerScreen'
import { ServerScreenViewModel } from './server/ServerScreenViewModel'
import { DevicesScreen } from './devices/devicesScreen'
import { PreferencesScreen } from './preferences/preferencesScreen'
import ThemeViewModel from '../theme/ThemeViewModel'
import { useUnwrap } from '../utils'

export default function App() {
    const themeViewModel = useMemo(() => new ThemeViewModel(), [])

    const preferredTheme = useUnwrap(themeViewModel.currentTheme)
    const isSystemInDarkMode = useMediaQuery('(prefers-color-scheme: dark)')

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode: preferredTheme == 'system' ? (isSystemInDarkMode ? 'dark' : 'light') : preferredTheme,
                },
                typography: { fontFamily: ['Inter Variable', 'Sans-serif'].join(',') },
            }),
        [isSystemInDarkMode, preferredTheme]
    )

    const location = useLocation().pathname

    useEffect(() => {
        console.log('Location changed: ' + location)
        return
    }, [location])

    const selected = getScreenFromRoute(location)

    return (
        <ThemeProvider theme={theme}>
            <Stack
                spacing={'0px'}
                whiteSpace={'0'}
                height='100%'
                direction={'row'}
                justifyContent={'center'}
                alignContent={'center'}
                flexWrap={'wrap'}
            >
                <AppDrawer
                    screenWidthPercentage='20%'
                    selectedScreen={selected}
                />

                <Routes>
                    <Route
                        path='/'
                        element={<ServerScreen vm={new ServerScreenViewModel()} />}
                    />
                    <Route
                        path='/devices'
                        element={<DevicesScreen />}
                    />
                    <Route
                        path='/preferences'
                        element={<PreferencesScreen></PreferencesScreen>}
                    />
                </Routes>
            </Stack>
        </ThemeProvider>
    )
}

function getScreenFromRoute(route: string): Screen {
    switch (route) {
        case '/':
            return Screen.SERVER_SCREEN
        case '/devices':
            return Screen.DEVICES_SCREEN
        case '/preferences':
            return Screen.PREFERENCES_SCREEN
    }
}
