

export type ServerState = 'running' | 'closed'

export type MainServerInitialized = {
    state: ServerState,
    port: number,
    serverName: string
}

export type MainServerState = 
    MainServerInitialized
    | 'init'
