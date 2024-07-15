import { BehaviorSubject, map } from 'rxjs'
import { AppPreferences, THEME, Theme, UI_PREFS } from '../../model/preferences'
import { MainProcessObservableConnection, subscribeToObservableInMainProcess } from '../utils'
import { mapBehaviorSubject } from '../../utilities/rxUtils'

export default class ThemeViewModel {
    currentTheme: BehaviorSubject<Theme>

    ipcConnection: MainProcessObservableConnection<Theme>

    constructor() {
        this.currentTheme = new BehaviorSubject('system')
        this.loadObservables()
    }

    private loadObservables = () => {
        const observableConnection = subscribeToObservableInMainProcess<AppPreferences>('prefs-state')
        observableConnection.observable.pipe(map((prefs) => prefs[UI_PREFS][THEME])).subscribe(this.currentTheme)
    }

    clean = () => {
        this.ipcConnection.clean()
    }
}
