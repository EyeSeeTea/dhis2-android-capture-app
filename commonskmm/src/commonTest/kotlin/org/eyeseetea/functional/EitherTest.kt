package org.eyeseetea.functional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EitherTest {
    @Test
    fun foldAppliesTheLeftFunctionWhenItIsALeft() {
        val result = givenALeft("boom").fold(fnL = { "left:$it" }, fnR = { "right:$it" })

        assertEquals("left:boom", result)
    }

    @Test
    fun foldAppliesTheRightFunctionWhenItIsARight() {
        val result = givenARight(2).fold(fnL = { "left:$it" }, fnR = { "right:$it" })

        assertEquals("right:2", result)
    }

    @Test
    fun mapTransformsTheValueOfARight() {
        val result = givenARight(2).map { it * 10 }

        assertEquals(Either.Right(20), result)
    }

    @Test
    fun mapKeepsALeftUntouched() {
        var mapperCalled = false

        val result =
            givenALeft("boom").map {
                mapperCalled = true
                it * 10
            }

        assertEquals(Either.Left("boom"), result)
        assertFalse(mapperCalled)
    }

    @Test
    fun flatMapChainsTheNextStepOfARight() {
        val result = givenARight(2).flatMap { Either.Right(it + 1) }

        assertEquals(Either.Right(3), result)
    }

    @Test
    fun flatMapCanTurnARightIntoALeft() {
        val result = givenARight(2).flatMap { Either.Left("too small") }

        assertEquals(Either.Left("too small"), result)
    }

    @Test
    fun flatMapDoesNotRunTheNextStepOfALeft() {
        var nextStepCalled = false

        val result =
            givenALeft("boom").flatMap {
                nextStepCalled = true
                Either.Right(it + 1)
            }

        assertEquals(Either.Left("boom"), result)
        assertFalse(nextStepCalled)
    }

    @Test
    fun getOrElseReturnsTheValueOfARight() {
        assertEquals(2, givenARight(2).getOrElse(17))
    }

    @Test
    fun getOrElseReturnsTheFallbackOfALeft() {
        assertEquals(17, givenALeft("boom").getOrElse(17))
    }

    @Test
    fun aRightIsRightAndNotLeft() {
        assertTrue(givenARight(2).isRight)
        assertFalse(givenARight(2).isLeft)
    }

    @Test
    fun aLeftIsLeftAndNotRight() {
        assertTrue(givenALeft("boom").isLeft)
        assertFalse(givenALeft("boom").isRight)
    }

    private fun givenARight(value: Int): Either<String, Int> = Either.Right(value)

    private fun givenALeft(error: String): Either<String, Int> = Either.Left(error)
}
