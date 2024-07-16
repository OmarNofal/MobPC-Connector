import { AppBehaviorPrefs, RUN_SERVER_ON_STARTUP, START_ON_LOGIN } from '../../../model/preferences'
import PreferencesHeader from '../../components/preferences/PreferencesHeader'
import { ToggleablePreference } from '../../components/preferences/ToggleablePreference'

type AppBehaviorPreferencesProps = {
    appBehaviorPrefs: AppBehaviorPrefs
    onToggleStartAppOnLogin: () => void
    onToggleRunServerOnStartup: () => void
}

export function AppBehaviorPreferences(props: AppBehaviorPreferencesProps) {
    return (
        <>
            <PreferencesHeader title='App Behavior' />
            <ToggleablePreference
                title='Open App on Login'
                subtitle='Launches the app in the tray when logging in'
                isToggled={props.appBehaviorPrefs[START_ON_LOGIN]}
                toggle={props.onToggleStartAppOnLogin}
                sx={{ marginTop: '32px' }}
            />
            <ToggleablePreference
                title='Run Server on Startup'
                subtitle='Runs the server when the app starts'
                isToggled={props.appBehaviorPrefs[RUN_SERVER_ON_STARTUP]}
                toggle={props.onToggleRunServerOnStartup}
                sx={{ marginTop: '28px' }}
            />
        </>
    )
}
