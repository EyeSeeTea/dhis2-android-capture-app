package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Covers the early session check added after the device test on 2026-09-25, where a 2FA user was
 * left for minutes on the Home after a restart, believing they were logged in.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionCheckOnStartTest {
    private var requests = 0

    @After
    fun tearDown() {
        SessionCheckOnStart.reset()
    }

    @Test
    fun `the server is asked once per app process`() = runTest {
        repeat(3) { givenAScreenOfTheAppResumes { requests++ } }

        assertEquals(1, requests)
    }

    @Test
    fun `an answer from the server counts, even a rejection`() = runTest {
        givenAScreenOfTheAppResumes {
            requests++
            error("401 Unauthorized")
        }
        givenAScreenOfTheAppResumes { requests++ }

        assertEquals(1, requests)
    }

    @Test
    fun `without connection the server is asked again on the next screen`() = runTest {
        givenAScreenOfTheAppResumes {
            requests++
            throw IOException("no connection")
        }
        givenAScreenOfTheAppResumes { requests++ }

        assertEquals(2, requests)
    }

    private fun TestScope.givenAScreenOfTheAppResumes(probe: suspend () -> Unit) {
        SessionCheckOnStart.checkOnce(CoroutineScope(UnconfinedTestDispatcher(testScheduler)), probe)
    }
}
