import { Stack, ThemeProvider, Typography, createTheme, useMediaQuery } from '@mui/material'
import AppDrawer from '../components/drawer/drawer'
import { Screen } from '../model/screens'
import {
    BrowserRouter,
    HashRouter,
    Link,
    MemoryRouter,
    Route,
    Router,
    Routes,
    Switch,
    useLocation,
} from 'react-router-dom'
import ServerScreen from './server/ServerScreen'
import { ServerScreenViewModel } from './server/ServerScreenViewModel'
import { useEffect, useMemo } from 'react'

const theme = createTheme({
    palette: {
        background: {},
    },
    typography: { fontFamily: ['Inter Variable', 'Sans-serif'].join(',') },
})

export default function App() {
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)')

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode: prefersDarkMode ? 'dark' : 'light',
                },
                typography: { fontFamily: ['Inter Variable', 'Sans-serif'].join(',') },
            }),
        [prefersDarkMode]
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
                        element={<Typography>Devices</Typography>}
                    />
                    <Route
                        path='/settings'
                        element={<Typography>Settings</Typography>}
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
        case '/settings':
            return Screen.SETTINGS_SCREEN
    }
}
