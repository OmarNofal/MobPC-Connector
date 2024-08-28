import { useState } from 'react'
import {
    FirebaseIPServiceConfiguration,
    GLOBAL_PORT,
    NAME,
    PORT,
    ServerConfiguration,
    ServerInformation,
    SYNC_IP_WITH_FIREBASE,
} from '../../../model/preferences'
import PreferencesHeader from '../../components/preferences/PreferencesHeader'
import PreferencesTextField from '../../components/preferences/PreferencesTextField'
import useTimedStateChange from '../../components/preferences/timedTextStateHolder'
import GlobalPortInformationDialogs from './InformationDialogs'
import { ToggleablePreference } from '../../components/preferences/ToggleablePreference'

type ServerConfigPreferencesProps = {
    serverInformation: ServerInformation
    serverConfiguration: ServerConfiguration
    firebaseConfiguration: FirebaseIPServiceConfiguration
    onServerNameChange: (name: string) => void
    onServerPortChange: (port: number) => void
    onGlobalPortChange: (port: number) => void
    onToggleFirebaseService: () => void
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
    const [globalPort, setGlobalPort] = useTimedStateChange(
        props.firebaseConfiguration[GLOBAL_PORT].toString(),
        1200,
        (newValue: string) => props.onGlobalPortChange(Number(newValue))
    )

    const [dialogOpen, setDialogOpen] = useState(false)

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

            <ToggleablePreference
                isToggled={props.firebaseConfiguration[SYNC_IP_WITH_FIREBASE]}
                title='Global Connectivity'
                subtitle='Synchronizes your IP address with Firebase to allow connections from anywhere'
                toggle={props.onToggleFirebaseService}
                sx={{ marginTop: '32px' }}
                infoIcon
                onInfoIconClicked={() => setDialogOpen(true)}
            />

            <GlobalPortInformationDialogs
                open={dialogOpen}
                closeDialog={() => setDialogOpen(false)}
            />
            <PreferencesTextField
                value={globalPort}
                title='Global Port'
                type='number'
                subtitle={`The port assigned on the router to forward outside requests to the server port (currently ${serverPort})`}
                onValueChange={setGlobalPort}
                sx={{ marginTop: '32px' }}
            />
        </>
    )
}
