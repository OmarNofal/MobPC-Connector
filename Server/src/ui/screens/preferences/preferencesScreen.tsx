import { Divider, Paper, Stack, Typography, useTheme } from '@mui/material'
import TopBar from '../../components/topbar'
import { useMemo, useState } from 'react'
import PreferencesScreenViewModel from './PreferencesScreenViewModel'
import { useUnwrap } from '../../utils'
import PreferencesHeader from '../../components/preferences/PreferencesHeader'
import PreferencesMultiselect from '../../components/preferences/PreferencesMultiselect'
import {
    APP_BEHAVIOR_PREFS,
    SERVER_CONFIGURATION,
    SERVER_INFORMATION,
    THEME,
    Theme,
    UI_PREFS,
} from '../../../model/preferences'
import { DisplayPreferences } from './displayPreferences'
import { AppBehaviorPreferences } from './appBehaviorPreferences'
import { ServerConfigPreferences } from './serverConfigPreferences'

export function PreferencesScreen() {
    const vm = useMemo(() => new PreferencesScreenViewModel(), [])
    const palette = useTheme().palette

    const value = useUnwrap(vm.state)

    if (value == 'loading') return <></>

    const prefs = value.preferences
    return (
        <Stack
            sx={{ flex: 1 }}
            direction={'column'}
            height={'100%'}
        >
            <TopBar
                title='Preferences'
                sx={{ bgcolor: palette.background.default }}
                divider
            />

            <Paper
                sx={{
                    height: '100%',
                    backgroundColor: palette.background.paper,
                    borderRadius: 0,
                    padding: '32px',
                    flex: '1',
                }}
                elevation={1}
            >
                <DisplayPreferences
                    uiPrefs={prefs[UI_PREFS]}
                    onThemeChanged={vm.changeTheme}
                />

                <Divider
                    sx={{ marginTop: '32px', width: 'auto', marginLeft: -32, marginRight: -4, marginBottom: '32px' }}
                />

                <AppBehaviorPreferences
                    appBehaviorPrefs={prefs[APP_BEHAVIOR_PREFS]}
                    onToggleStartAppOnLogin={vm.toggleStartAppOnLogin}
                    onToggleRunServerOnStartup={vm.toggleRunServerOnStartup}
                />

                <Divider
                    sx={{ marginTop: '32px', width: 'auto', marginLeft: -32, marginRight: -4, marginBottom: '32px' }}
                />

                <ServerConfigPreferences
                    serverInformation={prefs[SERVER_INFORMATION]}
                    serverConfiguration={prefs[SERVER_CONFIGURATION]}
                    onServerNameChange={vm.changeServerName}
                    onServerPortChange={vm.changeServerPort}
                />
            </Paper>
        </Stack>
    )
}
