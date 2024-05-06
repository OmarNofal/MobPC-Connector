import { CircularProgress, Divider, Paper, Stack, useTheme } from '@mui/material'
import NetworkInterfacesCard from '../../components/server/networkInterfaceInformation'
import ServerInformationCard from '../../components/server/serverInformation'
import TopBar from '../../components/topbar'
import { useUnwrap } from '../../utils'
import { ServerScreenInitializedState, ServerScreenViewModel } from './ServerScreenViewModel'

export default function ServerScreen(props: { vm: ServerScreenViewModel }) {
    const vm = props.vm

    const state = useUnwrap(vm.state)

    const isLoading = state == 'loading'

    const palette = useTheme().palette

    if (isLoading) {
        return <LoadingScreen />
    } else {
        return (
            <InitializedScreen
                state={state}
                onToggle={vm.toggleServer}
            />
        )
    }
}

function LoadingScreen() {
    return (
        <Paper
            sx={{
                backgroundColor: useTheme().palette.background.default,
                width: '100%',
                height: '100%'
            }}
        >
            <CircularProgress variant='indeterminate' />
        </Paper>
    )
}

function InitializedScreen(props: { state: ServerScreenInitializedState; onToggle: () => void }) {
    const state = props.state
    const palette = useTheme().palette

    return (
        <Stack
            sx={{ flex: 1 }}
            direction={'column'}
            height={'100%'}
        >
            <TopBar
                sx={{ bgcolor: palette.background.default }}
                title='Server'
            />

            <Divider />
            <Paper
                sx={{
                    width: '100%',
                    height: '100%',
                    backgroundColor: palette.background.paper,
                    borderRadius: 0,
                }}
                elevation={1}
            >
                <Stack
                    direction={'row'}
                    paddingTop={'24px'}
                    paddingRight={'24px'}
                    paddingLeft={'24px'}
                    spacing={'32px'}
                    justifyContent={'space-between'}
                >
                    <ServerInformationCard
                        sx={{
                            flex: 1,
                            maxWidth: '50%',
                        }}
                        serverState={state}
                        onToggleServer={props.onToggle}
                    />
                    <div
                        style={{
                            flex: 1,
                            maxWidth: '50%',
                            marginLeft: '16px',
                        }}
                    >
                        <NetworkInterfacesCard interfaces={state.networkInterfaces} />
                    </div>
                </Stack>
            </Paper>
        </Stack>
    )
}
