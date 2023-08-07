package com.omar.pcconnector.operation

import com.omar.pcconnector.network.api.AuthAPI
import com.omar.pcconnector.network.api.Token
import com.omar.pcconnector.network.api.getDataOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LoginOperation(
    private val api: AuthAPI,
    private val password: String
): Operation<Token>() {

    override val name: String
        get() = "Login operation"
    override val operationDescription: String
        get() = "Logging in"

    override suspend fun start(): Token = withContext(Dispatchers.IO) {
        api.login(password).getDataOrThrow()?.token ?: throw IllegalArgumentException("Empty token")
    }

}

class VerifyToken(
    private val api: AuthAPI,
    private val token: Token
): Operation<Boolean>() {

    override val name: String
        get() = "Login operation"
    override val operationDescription: String
        get() = "Logging in"

    override suspend fun start(): Boolean = withContext(Dispatchers.IO) {
        api.verifyToken(token).getDataOrThrow()?.isVerified ?: false
    }

}