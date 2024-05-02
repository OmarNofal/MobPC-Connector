import bcrypt from 'bcrypt'
import fs from 'fs'
import jwt from 'jsonwebtoken'
import path from 'path'
import { InvalidPasswordException, PasswordNotSetException } from './exceptions'
import credentialsManager from '../credentials/CredentialsManager'
import { PairingPayload } from './pairingPayload'

const pwdFileName = 'pwd'

/**
 * This class manages routines related to authentication
 * like updaing the password, generating access tokens, checking if a token is valid, etc...
 */
export default class AuthenticationManager {
    /**Directory where the password is stored */
    passwordDirectory: string

    /**
     * Path to the file containing the hashed password.
     *
     * This should be pointing in private app storage
     */
    passwordFilePath: string

    /**
     * The hash of the current password set by the user
     *
     * This is cached mainly to prevent excessive disk I/Os
     * when validating the token for each request
     */
    private passwordHash?: string

    /**@param passwordDirectory Directory where this class fetches and saves passwords */
    constructor(passwordDirectory: string) {
        this.passwordDirectory = passwordDirectory
        this.passwordFilePath = path.join(passwordDirectory, pwdFileName)
        console.log(this.passwordFilePath)
        this.passwordHash = this.readPasswordHash()
    }

    /**
     * Changes the password, which will implicitly log out all
     * registered devices.
     *
     * The new password must be at least 8 characters long
     * @param newPassword The new password to set
     */
    changePassword = (newPassword: string) => {
        if (newPassword.length < 8) throw new InvalidPasswordException('Password must be atleast 8 charchters long')

        const salt = bcrypt.genSaltSync()
        const encryptedPassword = bcrypt.hashSync(newPassword, salt)

        this.savePasswordHash(encryptedPassword)
        this.passwordHash = encryptedPassword
    }

    /**
     * Wheteher we have set a password or not
     *
     * This will return false when the app is started for the first time.
     *
     * If the password is not set, then all requests should be rejected untill
     * a password is set
     */
    isPasswordSet = () => {
        return this.passwordHash != undefined
    }

    /**
     * Checks the validity of the password, and creates a new
     * jsonwebtoken if the password is correct
     *
     * @param password The password to check
     * @returns A json web token if the password is correct
     */
    logInAndGetAccessToken = (password: string) => {
        let encryptedPassword = ''

        try {
            encryptedPassword = this.readPasswordHash()
        } catch (e) {
            if (!this.isPasswordSet()) throw new PasswordNotSetException('Password is not set')
            else throw e
        }

        console.log('pass: ' + password)
        console.log('ency pass: ' + encryptedPassword)
        const isCorrect = bcrypt.compareSync(password, encryptedPassword)

        if (isCorrect) {
            // we use the current hash password as the jwt secret,
            // so that we can make sure that a token is legitimate and healthy
            // if we can decrypt it using the current password
            return jwt.sign({}, this.passwordHash, { expiresIn: '30d' })
        } else {
            throw new InvalidPasswordException("Passwords don't match")
        }
    }

    /**
     * Checks if a token is valid to be used and not expired
     *
     * @returns true if the token is valid and not expired, false otherwise
     */
    isValidToken = (token: string) => {
        try {
            jwt.verify(token, this.passwordHash)
            return true
        } catch (e) {
            return false
        }
    }

    /**
     * Generates the payload containing information
     * for new devices to pair with this server.
     * The payload contains the server name, certificate,
     * ip addresses and port
     */
    generatePairingPaylod = (): PairingPayload => {
        const cert = credentialsManager.getCredentials().cert

        const name = 'Omar Nofal'
        const ip = ['192.168.1.76', '192.168.1.3']
        const port = 6543

        return {
            cert,
            name,
            ip,
            port,
        }
    }

    /**Saves the password hash to the filesystem */
    private savePasswordHash = async (hash: string) => {
        const buffer = Buffer.from(hash, 'utf-8')
        fs.writeFileSync(this.passwordFilePath, buffer)
    }

    /**Reads the password hash from the system */
    private readPasswordHash = () => {
        try {
            const data = fs.readFileSync(this.passwordFilePath)
            return data.toString('utf-8')
        } catch (e: any) {
            return undefined
        }
    }
}
