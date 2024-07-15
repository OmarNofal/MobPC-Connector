import { THEME, Theme, UI_PREFS, UiPreferences } from '../../../model/preferences'
import PreferencesHeader from '../../components/preferences/PreferencesHeader'
import PreferencesMultiselect from '../../components/preferences/PreferencesMultiselect'

export type DisplayPreferencesProps = {
    uiPrefs: UiPreferences
    onThemeChanged: (_: Theme) => void
}

const themes = ['Light', 'Dark', 'System']

export function DisplayPreferences({ uiPrefs, onThemeChanged }: DisplayPreferencesProps) {
    return (
        <>
            <PreferencesHeader title='Display' />
            <PreferencesMultiselect
                title='Theme'
                subtitle='The theme of the application'
                choices={themes}
                selectedIndex={themes.map((it) => it.toLowerCase()).indexOf(uiPrefs[THEME].toLowerCase())}
                sx={{ marginTop: '16px' }}
                onOptionSelected={(index) => {
                    onThemeChanged(themes[index].toLowerCase() as Theme)
                    console.log(`theme = ${themes[index].toLowerCase() as Theme}`)
                }}
            />
        </>
    )
}
