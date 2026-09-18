package org.dhis2.usescases.general;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.ActivityResultObservable;
import org.dhis2.commons.Constants;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.mobile.commons.reporting.CrashReportController;
import org.dhis2.usescases.notifications.domain.Notification;
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter;
import org.dhis2.usescases.notifications.presentation.NotificationsView;
import org.dhis2.usescases.notifications.presentation.ShowNotifications;
import org.dhis2.usescases.notifications.presentation.VisibleNotificationDialogs;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.noties.markwon.Markwon;
import kotlin.Unit;
import org.koin.java.KoinJavaComponent;


// EyeSeeTea customization - Notifications system
// NotificationsView on the class signature: this shared base activity is what renders the
// notification dialog for every screen of the authenticated area.
public abstract class ActivityGlobalAbstract extends SessionManagerActivity
        implements AbstractActivityContracts.View, ActivityResultObservable, NotificationsView {

    private static final String FRAGMENT_TAG = "SYNC";

    public String uuid;

    @Inject
    public CrashReportController crashReportController;

    // EyeSeeTea customization - Notifications system
    private NotificationsPresenter notificationsPresenter;

    // EyeSeeTea customization - Notifications system
    // One tracker per screen: a pending notification is offered again on every resume, so this
    // is what stops a second dialog being built on top of one that is already up.
    private final VisibleNotificationDialogs visibleNotificationDialogs =
            new VisibleNotificationDialogs();

    private CustomDialog descriptionDialog;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                ActivityGlobalAbstractExtensionsKt.wrappedContextForLanguage(
                        this,
                        ((App) newBase.getApplicationContext()).getServerComponent(),
                        newBase
                )
        );
    }

    // EyeSeeTea customization - Notifications system
    @Override
    protected void onResume() {
        super.onResume();
        NotificationsPresenter presenter = notificationsPresenter();
        if (presenter != null) {
            // Registered only while this screen is the one on top, so a notification arriving
            // from a background metadata sync is shown straight away instead of waiting for the
            // next resume. Cleared in onPause so the singleton never holds a paused Activity.
            ShowNotifications.INSTANCE.setOnPending(() -> {
                presenter.refresh(this);
                return Unit.INSTANCE;
            });
            presenter.refresh(this);
        }
    }

    // EyeSeeTea customization - Notifications system
    @Override
    protected void onPause() {
        ShowNotifications.INSTANCE.setOnPending(null);
        super.onPause();
    }

    // EyeSeeTea customization - Notifications system
    // Base behavior: this field does not exist upstream.
    // Resolved from Koin rather than field-injected by Dagger. Upstream migrated MainActivity to
    // Koin in 3.4.x and stopped running the inject() that used to populate this inherited field,
    // which left it null on every screen. Resolution is deferred and guarded on the server
    // component because this base class also backs the screens shown before there is a session,
    // and the notification graph needs an initialised D2.
    @Nullable
    protected NotificationsPresenter notificationsPresenter() {
        if (((App) getApplicationContext()).getServerComponent() == null) {
            return null;
        }
        if (notificationsPresenter == null) {
            notificationsPresenter = KoinJavaComponent.get(NotificationsPresenter.class);
        }
        return notificationsPresenter;
    }

    @Override
    public void setTutorial() {

    }

    @Override
    public void showTutorial(boolean shaked) {
        if (HelpManager.getInstance().isReady()) {
            HelpManager.getInstance().showHelp();
        } else {
            showToast(getString(R.string.no_intructions));
        }
    }

    public void showMoreOptions(View view) {
        new AppMenuHelper.Builder()
                .menu(this, R.menu.home_menu)
                .anchor(view)
                .onMenuInflated(popupMenu -> {
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(item -> {
                    getAnalyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(false);
                    return false;
                })
                .build()
                .show();
    }

    public Context getContext() {
        return this;
    }

    public ActivityGlobalAbstract getActivity() {
        return ActivityGlobalAbstract.this;
    }


    public ActivityGlobalAbstract getAbstracContext() {
        return this;
    }

    public ActivityGlobalAbstract getAbstractActivity() {
        return this;
    }

    public void back() {
        finish();
    }

    @Override
    public void displayMessage(String message) {
        if (message == null)
            message = getString(R.string.permission_denied);

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void renderError(String message) {
        showInfoDialog(getString(R.string.error), message);
    }

    @Override
    public void showInfoDialog(String title, String message) {
        if (getActivity() != null) {
            showInfoDialog(title, message, new OnDialogClickListener() {
                @Override
                public void onPositiveClick() {
                    // no-op
                }

                @Override
                public void onNegativeClick() {
                    // no-op
                }
            });
        }
    }

    @Override
    public void showInfoDialog(String title, String message, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            showInfoDialog(title, message, getString(R.string.button_ok), getString(R.string.cancel), clickListener);
        }
    }

    @Override
    public void showInfoDialog(String title, String message, String positiveButtonText, String negativeButtonText, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                    .setTitle(title)
                    .setCancelable(false)
                    .setMessage(message)
                    .setPositiveButton(positiveButtonText, (dialogInterface, i) -> clickListener.onPositiveClick())
                    .setNegativeButton(negativeButtonText, (dialogInterface, i) -> clickListener.onNegativeClick())
                    .show();
        }
    }


    @Override
    public void showDescription(String description) {
        if (descriptionDialog != null) {
            descriptionDialog.cancel();
        }
        descriptionDialog = new CustomDialog(
                getAbstracContext(),
                getString(R.string.info),
                description,
                getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
        );

        descriptionDialog.show();
    }

    @Override
    public void showSyncDialog(SyncStatusDialog dialog) {
        dialog.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    public AnalyticsHelper analyticsHelper() {
        return getAnalyticsHelper();
    }

    // EyeSeeTea customization - Notifications system
    // Dialog rendering for the notifications: Markwon for the Markdown body, the device locale
    // resolved against the translations map, and the accept button marking the notification read.
    @Override
    public void renderNotifications(List<Notification> notifications) {
        // The presenter dispatches this from a coroutine that is not tied to this Activity's
        // lifecycle, so it can arrive after a fast back press or a configuration change. Showing a
        // dialog on a dead Activity throws WindowManager.BadTokenException.
        if (isFinishing() || isDestroyed()) {
            return;
        }
        for (Notification notification : notifications) {
            showNotification(notification);
        }
    }

    private void showNotification(Notification notification) {
        // The notification stays pending until it is accepted, so this is called again on every
        // resume. Without this guard, resuming a screen whose dialog is still up builds a second
        // one on top: accepting the top copy leaves the others live, and each one marks the
        // notification as read again, appending a duplicate readBy entry on the server.
        if (visibleNotificationDialogs.isVisible(notification.getId())) {
            return;
        }

        NotificationsPresenter presenter = notificationsPresenter();
        String content = getNotificationContent(notification);
        Markwon markwon = Markwon.create(getContext());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle("Notification")
                .setMessage(content)
                .setPositiveButton(getContext().getString(R.string.wipe_data_ok), (d, which) -> {
                    if (presenter != null) {
                        presenter.markNotificationAsRead(notification);
                    }
                })
                // Released however the dialog goes away — accepted, back, or a tap outside — so a
                // notification dismissed without accepting is offered again on the next resume.
                .setOnDismissListener(d ->
                        visibleNotificationDialogs.onDismissed(notification.getId()))
                .setCancelable(true)
                .show();

        visibleNotificationDialogs.onShown(notification.getId());

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            markwon.setMarkdown(messageView, content);
        }
    }

    private String getNotificationContent(Notification notification) {
        String language = Locale.getDefault().getLanguage();
        if (notification.getTranslations() != null && notification.getTranslations().containsKey(language)) {
            return notification.getTranslations().get(language);
        }
        return notification.getContent();
    }
}
