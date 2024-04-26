import { BrowserWindow } from "electron";
import { BehaviorSubject, Observable, Subscription } from "rxjs";
import { pipeObservableToIPC } from "../utilities/rxIPC";




type IPCObservable = {
    observable: Observable<any>,
    channelName: string
}


/**
 * This is the main browser window of the application.
 * 
 * This class manages the interaction between the main process and the window.
 * 
 * 
 */
export default class AppWindow extends BrowserWindow {


    /**
     * Maintains all subsciption of obseravbles
     * that are pushed through IPC channel
     */
    private subscriptions: Subscription[]
    
    
    /**
     * 
     * @param options Window options
     * @param observables The observables that should be pushed to the renderer process
     * when they update
     */
    constructor(
        options: Electron.BrowserWindowConstructorOptions,
        observables: IPCObservable[]
    ) {
        super(options)
        
        this.setupObservables(observables)
        this.on('close', () => this.cleanUpObservables())
    }


    setupObservables =  (observables: IPCObservable[]) => {
        this.subscriptions = observables.map(
            (o: IPCObservable) => pipeObservableToIPC(o.observable, this.webContents, o.channelName) 
        )
    }

    cleanUpObservables = () => {
        this.subscriptions.forEach(v => v.unsubscribe())
    }
    
}