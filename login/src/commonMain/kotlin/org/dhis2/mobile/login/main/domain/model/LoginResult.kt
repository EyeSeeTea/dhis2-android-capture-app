package org.dhis2.mobile.login.main.domain.model

sealed interface LoginResult {
    data class Success(
        val displayTrackingMessage: Boolean,
        val initialSyncDone: Boolean,
    ) : LoginResult

    data class Error(
        val message: String?,
    ) : LoginResult

    /**
     * EyeSeeTea customization - 2FA support
     * The server's answer about two factor authentication: a code is required, was sent, or the
     * code submitted was rejected. Not always an error, so it is not named as one.
     */
    data class TwoFactor(
        val outcome: TwoFactorOutcome,
    ) : LoginResult
}
