

export type ServerState = 'running' | 'closed'

export type MainServerInitialized = {
    state: ServerState,
    httpsPort: number,
    httpPort: number,
    serverName: string
}

export type MainServerState = 
    MainServerInitialized
    | 'init'
