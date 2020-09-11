package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.types.TreeNode
import timber.log.Timber

sealed class FeedbackContentState {
    object Loading : FeedbackContentState()
    data class Loaded(val feedback: List<TreeNode<*>>, val onlyFailedFilter: Boolean) :
        FeedbackContentState()

    object NotFound : FeedbackContentState()
    object UnexpectedError : FeedbackContentState()
}

class FeedbackContentPresenter(private val getFeedback: GetFeedback) :
    CoroutineScope by MainScope() {

    private var view: FeedbackContentView? = null
    private lateinit var feedbackMode: FeedbackMode
    private var criticalFilter: Boolean? = null
    private var lastLoaded: FeedbackContentState.Loaded? = null

    fun attach(
        view: FeedbackContentView,
        feedbackMode: FeedbackMode,
        criticalFilter: Boolean?,
        onlyFailedFilter: Boolean
    ) {
        this.view = view
        this.feedbackMode = feedbackMode
        this.criticalFilter = criticalFilter

        loadFeedback(onlyFailedFilter)
    }

    fun detach() {
        this.view = null
        cancel()
    }

    fun changeOnlyFailedFilter(value: Boolean) {
        loadFeedback(value)
    }

    private fun loadFeedback(onlyFailedFilter: Boolean) = launch {
        render(FeedbackContentState.Loading)

        val result = withContext(Dispatchers.IO) {
            getFeedback(feedbackMode, criticalFilter, onlyFailedFilter)
        }

        result.fold(
            { failure -> handleFailure(failure) },
            { feedback ->
                lastLoaded = FeedbackContentState.Loaded(feedback, onlyFailedFilter)
                render(lastLoaded!!)
            })
    }

    private fun handleFailure(failure: FeedbackFailure) {
        when (failure) {
            is FeedbackFailure.NotFound -> render(FeedbackContentState.NotFound)
            is FeedbackFailure.UnexpectedError -> {
                render(FeedbackContentState.UnexpectedError)
                Timber.d(failure.error)
            }
        }
    }

    private fun render(state: FeedbackContentState) {
        view?.render(state)
    }

    interface FeedbackContentView {
        fun render(state: FeedbackContentState)
    }
}