package org.dhis2.mobile.login.main.domain.model

/**
 * EyeSeeTea customization - 2FA support
 * What the server answered about two factor authentication, for the [type] of 2FA the user has.
 * The same channel produces all three, so each outcome is its own type instead of a flag on a
 * shared one.
 */
sealed interface TwoFactorOutcome {
    val type: TwoFactorType

    /** Message from the repository, to be shown to the user. */
    val message: String?

    /** The server asks for a code and none has been submitted yet. */
    data class CodeRequired(
        override val type: TwoFactorType,
        override val message: String? = null,
    ) : TwoFactorOutcome

    /** The server has just sent a code, by email or SMS. */
    data class CodeSent(
        override val type: TwoFactorType,
        override val message: String? = null,
    ) : TwoFactorOutcome

    /** A code was submitted and the server refused it. */
    data class CodeRejected(
        override val type: TwoFactorType,
        override val message: String? = null,
    ) : TwoFactorOutcome
}

class TwoFactorRequiredException(
    val outcome: TwoFactorOutcome,
) : Exception("Two factor authentication required: ${outcome.type}")
