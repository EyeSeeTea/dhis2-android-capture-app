package org.dhis2.utils.session
// EyeSeeTea customization - Change Server URL

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.commons.providers.SECURE_SERVER_URL
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.D2
import timber.log.Timber

enum class Mode {
    EDIT, WARNING
}

class ChangeServerURLPresenter(
    private val view: ChangeServerURLView,
    private val preferenceProvider: PreferenceProvider,
    val d2: D2
) {

    private var currentServerURL: String = ""
    private var newServerURL: String = ""
    private var mode = Mode.EDIT

    fun init() {
        val serverURL = preferenceProvider.getString(SECURE_SERVER_URL) ?: ""

        this.currentServerURL = serverURL.replace("/api", "")

        view.renderServerUrl(currentServerURL)

        view.disableOk()
    }

    fun onServerChanged(serverUrl: CharSequence, start: Int, before: Int, count: Int) {
        this.newServerURL = serverUrl.toString()

        if (serverUrl.isNotEmpty() && serverUrl.toString() != currentServerURL) {
            view.enableOk()
        } else {
            view.disableOk()
        }
    }

    fun save() {
        if (mode == Mode.EDIT) {
            if (currentServerURL != newServerURL) {
                mode = Mode.WARNING
                view.requestConfirmation()
            }
        } else {
            saveInTheStore()
        }
    }

    private fun saveInTheStore() {
        view.showLoginProgress()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateUrlInPreference()
                updateCredentialsAndDataBaseConfigurations()

                d2.databaseAdapter().execSQL("DELETE FROM SystemInfo")

                // Awaited here, in the same coroutine, instead of in a detached scope: the local
                // SystemInfo has just been deleted, so reporting success before the new server has
                // answered would tell the user the change worked when it may not have. Running it
                // here also puts any failure inside the catch below.
                d2.systemInfoModule().systemInfo().download().blockingAwait()

                launch(Dispatchers.Main) {
                    view.renderSuccess("Server changed successfully to $newServerURL")
                    view.closeDialog()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    handleError(e)
                }
            }
        }
    }

    private fun updateUrlInPreference() {
        //preferenceProvider.setValue(SERVER, newServerURL)

        preferenceProvider.updateServerURL(newServerURL)

        val updatedServer =
            (preferenceProvider.getSet(Constants.PREFS_URLS, HashSet()) as HashSet)

        updatedServer.remove(currentServerURL)
        updatedServer.add(newServerURL)

        preferenceProvider.setValue(Constants.PREFS_URLS, updatedServer)
    }

    private fun updateCredentialsAndDataBaseConfigurations() {
        d2.userModule().accountManager().changeServerUrl(newServerURL)
    }

    private fun handleError(error: Throwable) {
        Timber.e(error)

        // Back to EDIT along with the view: leaving it on WARNING would make the next OK skip the
        // confirmation dialog and re-apply straight away. Unreachable until the awaited download
        // started reporting failures instead of swallowing them.
        mode = Mode.EDIT

        view.renderError(error)
        view.hideLoginProgress()
        view.showEditMode()
    }
}

interface ChangeServerURLView {
    fun showEditMode()
    fun requestConfirmation()
    fun renderServerUrl(url: String)
    fun enableOk()
    fun disableOk()
    fun getAbstractContext(): ActivityGlobalAbstract
    fun showLoginProgress()
    fun hideLoginProgress()
    fun renderError(throwable: Throwable)
    fun renderSuccess(message: String)
    fun closeDialog()
}
