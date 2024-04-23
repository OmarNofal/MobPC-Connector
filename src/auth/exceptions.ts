

export class InvalidPasswordException extends Error {
    constructor(message) {
      super(message);
      this.name = 'Invalid password';
    }
  }

export class PasswordNotSetException extends Error {
  constructor(message) {
    super(message);
    this.name = 'Invalid password';
  }
}

