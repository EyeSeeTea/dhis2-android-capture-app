package org.dhis2.usescases.general;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import org.dhis2.data.server.ServerComponent;
import org.dhis2.usescases.notifications.domain.Notification;
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter;
import org.dhis2.usescases.notifications.presentation.NotificationsView;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.koin.java.KoinJavaComponent;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.noties.markwon.Markwon;
import kotlin.Unit;


public abstract class ActivityGlobalAbstract extends SessionManagerActivity
        implements AbstractActivityContracts.View, ActivityResultObservable, NotificationsView {

    private static final String FRAGMENT_TAG = "SYNC";

    public String uuid;

    @Inject
    public CrashReportController crashReportController;

    // EyeSeeTea customization - Notifications system
    private NotificationsPresenter notificationsPresenter;

    private CustomDialog descriptionDialog;

    // EyeSeeTea customization - Notifications system
    // Resolved from Koin instead of injected: upstream activities no longer run a Dagger
    // inject() that would populate an inherited field. Kotlin subclasses keep reading this
    // as the `notificationsPresenter` synthetic property, so their call sites are unchanged.
    public NotificationsPresenter getNotificationsPresenter() {
        if (notificationsPresenter == null) {
            notificationsPresenter = KoinJavaComponent.get(NotificationsPresenter.class);
        }
        return notificationsPresenter;
    }

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ServerComponent serverComponent = ((App) getApplicationContext()).getServerComponent();

        // EyeSeeTea customization - Notifications system
        getNotificationsPresenter().refresh(this);

        super.onCreate(savedInstanceState);
    }

    // EyeSeeTea customization - Notifications system
    @Override
    protected void onResume() {
        super.onResume();
        getNotificationsPresenter().refresh(this);
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

    @Override
    public void renderNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            showNotification(notification);
        }
    }

    private void showNotification(Notification notification) {
        String content = getNotificationContent(notification);
        Markwon markwon = Markwon.create(getContext());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle("Notification")
                .setMessage(content)
                .setPositiveButton(getContext().getString(R.string.wipe_data_ok), (d, which) -> {
                    getNotificationsPresenter().markNotificationAsRead(notification);
                })
                .setCancelable(true)
                .show();

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
