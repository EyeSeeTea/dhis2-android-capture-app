// EyeSeeTea customization - Either
package org.eyeseetea.functional

/**
 * A value of one of two possible types. By convention [Left] is the expected failure and
 * [Right] the success, and the operators are right-biased: they act on a [Right] and
 * leave a [Left] untouched.
 *
 * Use it in the domain for outcomes the caller must handle (see `Result` for what is
 * unexpected): the failure is a typed value, not an exception.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>()

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>()

    val isLeft: Boolean get() = this is Left

    val isRight: Boolean get() = this is Right

    fun <T> fold(
        fnL: (L) -> T,
        fnR: (R) -> T,
    ): T =
        when (this) {
            is Left -> fnL(value)
            is Right -> fnR(value)
        }
}

fun <L, R, T> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> fn(value)
    }

fun <L, R, T> Either<L, R>.map(fn: (R) -> T): Either<L, T> = flatMap { Either.Right(fn(it)) }

fun <L, R> Either<L, R>.getOrElse(default: R): R = fold(fnL = { default }, fnR = { it })
