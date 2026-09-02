package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class LoginUser(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        isNetworkAvailable: Boolean,
        twoFactorCode: String? = null,
    ): LoginResult {
        val trimmedUsername = username.trim()
        // EyeSeeTea customization - 2FA support
        val result =
            repository.loginUser(serverUrl, trimmedUsername, password, isNetworkAvailable, twoFactorCode)
        return handleResult(result, serverUrl, trimmedUsername)
    }
}
