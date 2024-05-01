import { app } from 'electron'
import path from 'path'
import forge from 'node-forge'
import fs from 'fs'

type Credentials = {
    cert: string
    privateKey: string
}

/**
 * This class is responsible for generating, saving and retreiving the self-signed
 * certificate for the `https` server.
 *
 * Initialy, the class will generate a certificate and save it to the app directory
 * and then it will reuse this certificate until it is deleted.
 */
export default class CredentialsManager {
    /**
     * Path to directory containing the certificate
     */
    private certificateDir: string

    private credentials: Credentials

    constructor() {
        this.certificateDir = path.join(app.getPath('userData'), 'cert')
        if (!fs.existsSync(this.certificateDir)) {
            fs.mkdirSync(this.certificateDir, { recursive: true })
        }

        this.credentials = this.getOrCreateCredentials()
    }

    getCredentials = () => {
        return this.credentials
    }

    /**
     * @returns the current credentials or saves and returns new credentials
     */
    private getOrCreateCredentials = (): Credentials => {
        const certFilePath = path.join(this.certificateDir, 'cert')
        const keyFilePath = path.join(this.certificateDir, 'key')

        try {
            const certContents = fs.readFileSync(certFilePath, { encoding: 'utf-8' })
            const keyContents = fs.readFileSync(keyFilePath, { encoding: 'utf-8' })

            return { cert: certContents, privateKey: keyContents }
        } catch {
            const newCredentials = this.generateCertificate()

            fs.writeFileSync(certFilePath, newCredentials.cert, { flag: 'w' })
            fs.writeFileSync(keyFilePath, newCredentials.privateKey, { flag: 'w' })

            return newCredentials
        }
    }

    /**
     * Generates a new self-signed SSL certificate valid forever
     *
     * @returns object containing the certificate with its private key
     */
    private generateCertificate(): Credentials {
        const { privateKey, publicKey } = forge.pki.rsa.generateKeyPair(2048)
        const cert = forge.pki.createCertificate()

        cert.publicKey = publicKey

        cert.serialNumber = '01'

        cert.validity.notBefore = new Date()

        const infiniteDate = new Date()
        infiniteDate.setFullYear(9999)
        cert.validity.notAfter = infiniteDate

        const attrs = [
            {
                name: 'countryName',
                value: 'EG',
            },
            {
                shortName: 'ST',
                value: 'Cairo',
            },
            {
                name: 'organizationName',
                value: 'Home Web Server',
            },
        ]

        cert.setIssuer(attrs)
        cert.setSubject(attrs)

        cert.sign(privateKey)

        const certPem = forge.pki.certificateToPem(cert)
        const pkeyPem = forge.pki.privateKeyToPem(privateKey)

        return {
            cert: certPem,
            privateKey: pkeyPem,
        }
    }
}
