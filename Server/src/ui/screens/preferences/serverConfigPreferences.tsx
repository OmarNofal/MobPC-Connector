import {
    NAME,
    PORT,
    RUN_SERVER_ON_STARTUP,
    ServerConfiguration,
    ServerInformation,
    START_ON_LOGIN,
} from '../../../model/preferences'
import PreferencesHeader from '../../components/preferences/PreferencesHeader'
import PreferencesTextField from '../../components/preferences/PreferencesTextField'
import useTimedStateChange from '../../components/preferences/timedTextStateHolder'
import { ToggleablePreference } from '../../components/preferences/ToggleablePreference'

type ServerConfigPreferencesProps = {
    serverInformation: ServerInformation
    serverConfiguration: ServerConfiguration
    onServerNameChange: (name: string) => void
    onServerPortChange: (port: number) => void
}

export function ServerConfigPreferences(props: ServerConfigPreferencesProps) {
    const [serverName, setServerName] = useTimedStateChange(
        props.serverInformation[NAME],
        1200,
        props.onServerNameChange
    )
    const [serverPort, setServerPort] = useTimedStateChange(
        props.serverConfiguration[PORT].toString(),
        1200,
        (newValue: string) => props.onServerPortChange(Number(newValue))
    )

    return (
        <>
            <PreferencesHeader title='Server Preferences' />

            <PreferencesTextField
                value={serverName.toString()}
                title='Server Name'
                subtitle='The name of this server to appear on other devices'
                onValueChange={setServerName}
                sx={{ marginTop: '32px' }}
                type='text'
            />
            <PreferencesTextField
                value={serverPort}
                title='Server Port'
                type='number'
                subtitle='The port to use for the server. Must be between 49152 & 65535  (Changing this will restart the server)'
                onValueChange={setServerPort}
                sx={{ marginTop: '32px' }}
            />
        </>
    )
}
