package org.dhis2.utils.session

import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.commons.providers.SECURE_SERVER_URL
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChangeServerURLPresenterTest {

    private val view: ChangeServerURLView = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private lateinit var presenter: ChangeServerURLPresenter

    @Before
    fun setUp() {
        presenter = ChangeServerURLPresenter(view, preferenceProvider, d2)
    }

    @Test
    fun `should render current server URL without api suffix on init`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://example.com/api"

        presenter.init()

        verify(view).renderServerUrl("https://example.com")
        verify(view).disableOk()
    }

    @Test
    fun `should render empty URL when no server URL is stored`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn null

        presenter.init()

        verify(view).renderServerUrl("")
        verify(view).disableOk()
    }

    @Test
    fun `should enable ok button when user enters a different non-empty URL`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://old.example.com/api"
        presenter.init()

        presenter.onServerChanged("https://new.example.com", 0, 0, 23)

        verify(view).enableOk()
    }

    @Test
    fun `should disable ok button when user enters empty URL`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://old.example.com/api"
        presenter.init()

        presenter.onServerChanged("", 0, 0, 0)

        // disableOk called on init + on empty input
        verify(view, Mockito.times(2)).disableOk()
    }

    @Test
    fun `should disable ok button when user enters same URL as current`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://example.com/api"
        presenter.init()

        presenter.onServerChanged("https://example.com", 0, 0, 20)

        // disableOk called on init + on same URL
        verify(view, Mockito.times(2)).disableOk()
    }

    @Test
    fun `should request confirmation on first save when URL has changed`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://old.example.com/api"
        presenter.init()

        presenter.onServerChanged("https://new.example.com", 0, 0, 23)
        presenter.save()

        verify(view).requestConfirmation()
        verify(view, never()).showLoginProgress()
    }

    @Test
    fun `should not request confirmation when URL has not changed`() {
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn
            "https://example.com/api"
        presenter.init()

        // Set newServerURL to match currentServerURL so save() does nothing
        presenter.onServerChanged("https://example.com", 0, 0, 20)
        presenter.save()

        verify(view, never()).requestConfirmation()
        verify(view, never()).showLoginProgress()
    }
}
