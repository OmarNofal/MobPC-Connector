import { BehaviorSubject } from 'rxjs'
import {
    APP_BEHAVIOR_PREFS,
    AppPreferences,
    NAME,
    PORT,
    RUN_SERVER_ON_STARTUP,
    SERVER_CONFIGURATION,
    SERVER_INFORMATION,
    START_ON_LOGIN,
    THEME,
    Theme,
    UI_PREFS,
} from '../../../model/preferences'
import { MainProcessObservableConnection, subscribeToObservableInMainProcess } from '../../utils'

export type PrefsScreenState =
    | {
          preferences: AppPreferences
      }
    | 'loading'

export default class PreferencesScreenViewModel {
    state: BehaviorSubject<PrefsScreenState>

    observableConnection: MainProcessObservableConnection<AppPreferences>

    constructor() {
        this.state = new BehaviorSubject('loading')
        this.loadObservables()
    }

    loadObservables = () => {
        this.observableConnection = subscribeToObservableInMainProcess<AppPreferences>('prefs-state')
        console.log('loadObservables')
        this.observableConnection.observable.subscribe((appPreferences: AppPreferences) => {
            console.log('New prefs: ' + appPreferences)
            this.state.next({ preferences: appPreferences })
        })
    }

    toggleStartAppOnLogin = () => {
        if (this.state.value == 'loading') return
        console.log('toggling')
        window.prefs.updatePreferencesKey(
            APP_BEHAVIOR_PREFS,
            START_ON_LOGIN,
            !this.state.value.preferences[APP_BEHAVIOR_PREFS][START_ON_LOGIN]
        )
    }

    toggleRunServerOnStartup = () => {
        if (this.state.value == 'loading') return
        window.prefs.updatePreferencesKey(
            APP_BEHAVIOR_PREFS,
            RUN_SERVER_ON_STARTUP,
            !this.state.value.preferences[APP_BEHAVIOR_PREFS][RUN_SERVER_ON_STARTUP]
        )
    }

    changeTheme = (theme: Theme) => {
        window.prefs.updatePreferencesKey(UI_PREFS, THEME, theme)
    }

    changeServerName = (name: string) => {
        window.prefs.updatePreferencesKey(SERVER_INFORMATION, NAME, name)
    }

    changeServerPort = (port: number) => {
        window.prefs.updatePreferencesKey(SERVER_CONFIGURATION, PORT, port)
    }
}
