import { BehaviorSubject } from 'rxjs'
import { AppPreferences, THEME, Theme, UI_PREFS } from '../../../model/preferences'
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

    changeTheme = (theme: Theme) => {
        window.prefs.updatePreferencesKey(UI_PREFS, THEME, theme)
    }
}
