package org.dhis2.mobile.login.main.domain.model

/**
 * EyeSeeTea customization - 2FA support
 * @param type The type of 2FA required (TOTP, EMAIL, SMS)
 * @param errorMessage Message from the repository (can be error or info message)
 * @param codeSent True when the server has just sent a code, false when it rejected one. The
 * channel alone cannot tell them apart: an EMAIL challenge and a wrong EMAIL code arrive with
 * the same type, and only this flag says which is which.
 */
class TwoFactorRequiredException(
    val type: TwoFactorType,
    val errorMessage: String? = null,
    val codeSent: Boolean = false,
) : Exception("Two factor authentication required: $type")



