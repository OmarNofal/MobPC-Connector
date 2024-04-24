"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const bcrypt_1 = __importDefault(require("bcrypt"));
const fs_1 = __importDefault(require("fs"));
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const path_1 = __importDefault(require("path"));
const exceptions_1 = require("./exceptions");
const pwdFileName = "pwd";
/**
 * This class manages routines related to authentication
 * like updaing the password, generating access tokens, checking if a token is valid, etc...
 */
class AuthenticationManager {
    /**@param passwordDirectory Directory where this class fetches and saves passwords */
    constructor(passwordDirectory) {
        /**
         * Changes the password, which will implicitly log out all
         * registered devices.
         *
         * The new password must be at least 8 characters long
         * @param newPassword The new password to set
         */
        this.changePassword = (newPassword) => {
            if (newPassword.length < 8)
                throw new exceptions_1.InvalidPasswordException("Password must be atleast 8 charchters long");
            const salt = bcrypt_1.default.genSaltSync();
            const encryptedPassword = bcrypt_1.default.hashSync(newPassword, salt);
            this.savePasswordHash(encryptedPassword);
            this.passwordHash = encryptedPassword;
        };
        /**
         * Wheteher we have set a password or not
         *
         * This will return false when the app is started for the first time.
         *
         * If the password is not set, then all requests should be rejected untill
         * a password is set
        */
        this.isPasswordSet = () => {
            return this.passwordHash != undefined;
        };
        /**
         * Checks the validity of the password, and creates a new
         * jsonwebtoken if the password is correct
         *
         * @param password The password to check
         * @returns A json web token if the password is correct
         */
        this.logInAndGetAccessToken = (password) => {
            let encryptedPassword = "";
            try {
                encryptedPassword = this.readPasswordHash();
            }
            catch (e) {
                if (!this.isPasswordSet())
                    throw new exceptions_1.PasswordNotSetException("Password is not set");
                else
                    throw e;
            }
            console.log('pass: ' + password);
            console.log("ency pass: " + encryptedPassword);
            const isCorrect = bcrypt_1.default.compareSync(password, encryptedPassword);
            if (isCorrect) {
                // we use the current hash password as the jwt secret, 
                // so that we can make sure that a token is legitimate and healthy  
                // if we can decrypt it using the current password
                return jsonwebtoken_1.default.sign({}, this.passwordHash, { expiresIn: "30d" });
            }
            else {
                throw new exceptions_1.InvalidPasswordException("Passwords don't match");
            }
        };
        /**
         * Checks if a token is valid to be used and not expired
         *
         * @returns true if the token is valid and not expired, false otherwise
         */
        this.isValidToken = (token) => {
            try {
                jsonwebtoken_1.default.verify(token, this.passwordHash);
                return true;
            }
            catch (e) {
                return false;
            }
        };
        /**Saves the password hash to the filesystem */
        this.savePasswordHash = (hash) => __awaiter(this, void 0, void 0, function* () {
            const buffer = Buffer.from(hash, "utf-8");
            fs_1.default.writeFileSync(this.passwordFilePath, buffer);
        });
        /**Reads the password hash from the system */
        this.readPasswordHash = () => {
            try {
                const data = fs_1.default.readFileSync(this.passwordFilePath);
                return data.toString("utf-8");
            }
            catch (e) {
                return undefined;
            }
        };
        this.passwordDirectory = passwordDirectory;
        this.passwordFilePath = path_1.default.join(passwordDirectory, pwdFileName);
        console.log(this.passwordFilePath);
        this.passwordHash = this.readPasswordHash();
    }
}
exports.default = AuthenticationManager;
