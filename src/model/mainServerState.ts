


export const MainServerInitState = {
    state: 'init'
}

export const MainServerClosedState = {
    state: 'closed'
}

export type MainServerRunningState = {
    state: 'running',
    port: number
}

export type MainServerState = 
    MainServerRunningState
    | typeof MainServerClosedState
    | typeof MainServerInitState
