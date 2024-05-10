import { networkInterfaces } from 'os'
import { BehaviorSubject } from 'rxjs'

export type NetworkInterface = {
    name: string
    ipv4: string
}

const networkInterfacesObservable = new BehaviorSubject<NetworkInterface[]>([])

function updateState() {
    const interfaces = networkInterfaces()
    const newValue: NetworkInterface[] = []

    for (const name of Object.keys(interfaces)) {
        for (const net of interfaces[name]) {
            // Push IPv4 addresses to the array
            if (net.family === 'IPv4' && net.address != '127.0.0.1') {
                newValue.push({
                    name: name,
                    ipv4: net.address,
                })
            }
        }
    }

    networkInterfacesObservable.next(newValue)
}

export default function observeNetworkInterfaces() {
    setInterval(() => {
        updateState()
    }, 60_000) // refresh every minute
    updateState()
    return networkInterfacesObservable
}
