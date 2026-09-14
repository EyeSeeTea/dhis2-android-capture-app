package org.dhis2.usescases.datasets.datasetDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DataSetDetailViewModelTest {
    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private val dispatcher: DispatcherProvider =
        mock {
            // EyeSeeTea fix - DataSetDetailViewModelTest leaks a coroutine into the next test (no Oslo ticket, introduced 3.0)
            // Remove when Oslo ships the upstream fix.
            // With the real Dispatchers.IO, the ViewModel's withContext(io) could still be running
            // when @After resets Dispatchers.Main; resuming on Main then threw, and
            // kotlinx-coroutines-test reported it as UncaughtExceptionsBeforeTest in whichever
            // test ran next (usually MainViewModelIntegrationTest or MainViewModelTest).
            on { io() } doReturn UnconfinedTestDispatcher()
        }
    private val dataSetPageConfigurator: DataSetPageConfigurator = mock()
    private val initializedConfigurator: DataSetPageConfigurator = mock()

    private lateinit var viewModel: DataSetDetailViewModel

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `Should init variables of page configurator`() {
        whenever(dataSetPageConfigurator.initVariables()) doReturn initializedConfigurator

        viewModel =
            DataSetDetailViewModel(
                dispatcher,
                dataSetPageConfigurator,
            )
        viewModel.pageConfiguration.observeForever { result ->
            assertEquals(result, initializedConfigurator)
        }
    }
}
