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
        // EyeSeeTea customization - 2FA support
        twoFactorCode: String? = null,
    ): LoginResult {
        val urlWithScheme =
            if (serverUrl.startsWith("https://") || serverUrl.startsWith("http://")) {
                serverUrl
            } else {
                "https://$serverUrl"
            }
        // EyeSeeTea customization - 2FA support
        val result = repository.loginUser(urlWithScheme, username, password, twoFactorCode)
        return handleResult(result, urlWithScheme, username)
    }
}
