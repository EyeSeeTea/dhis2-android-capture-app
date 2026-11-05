# Notifications system

## Purpose

WIDP needs a lightweight in-app notification channel so that programme coordinators can push announcements, reminders, and operational messages to field users without relying on a separate messaging service. Notifications are authored in the DHIS2 datastore and delivered through the existing sync flow.

## Requirements

### Requirement: Notifications are fetched from the DHIS2 datastore
The app SHALL fetch the notification list from the DHIS2 server at `dataStore/notifications/notifications` during metadata sync, and SHALL store the result locally after filtering.

#### Scenario: Metadata sync retrieves notifications
- **WHEN** a metadata sync runs
- **THEN** the app issues a `GET dataStore/notifications/notifications` request and parses the response as a list of notification objects, each with `id`, `content`, `translations`, `recipients`, and `readBy` fields

#### Scenario: Datastore endpoint is unreachable
- **WHEN** the datastore endpoint returns an error or is not reachable
- **THEN** the metadata sync SHALL complete without failing, SHALL leave the previously stored notifications untouched, and SHALL NOT mark notifications as pending

A failed fetch SHALL be distinguishable from a datastore that is genuinely empty: only the latter replaces the stored notifications.

#### Scenario: User groups cannot be read
- **WHEN** the notifications are fetched but the lookup of the current user's groups (`users/{id}?fields=userGroups`) returns an error or is not reachable
- **THEN** the metadata sync SHALL complete without failing, SHALL leave the previously stored notifications untouched, and SHALL NOT mark notifications as pending, because filtering without the user's groups would drop every group-targeted notification

### Requirement: Notifications are filtered for the current user
The app SHALL store only notifications that are relevant to the currently logged-in user, by applying the following combined filter during sync.

A notification is relevant when BOTH of the following hold:
1. The current user's ID is NOT already present in the notification's `readBy` list.
2. AND at least one of the following recipient conditions matches:
   - The notification's wildcard is `"ALL"` (case-insensitive).
   - OR the notification's `recipients.userGroups` intersects the current user's group memberships, AND the notification targets Android (wildcard is `"Android"`, `"Both"`, or empty).
   - OR the current user's ID is in `recipients.users`, AND the notification targets Android (wildcard is `"Android"`, `"Both"`, or empty).

The accepted values for "targets Android" are `"Android"`, `"Both"`, `""` (empty), and `"ALL"` (any casing). Any other value — including `"Web"` — SHALL be rejected.

#### Scenario: Broadcast notification with wildcard ALL
- **WHEN** a notification has wildcard `"ALL"` and the current user has not read it
- **THEN** the notification is kept after filtering

#### Scenario: User-group targeted notification for Android
- **WHEN** a notification targets user group `G1` with wildcard `"Android"`, and the current user belongs to `G1`
- **THEN** the notification is kept after filtering

#### Scenario: User-group targeted notification for Web only
- **WHEN** a notification targets user group `G1` with wildcard `"Web"`, and the current user belongs to `G1`
- **THEN** the notification is filtered out

#### Scenario: Direct user match on Android
- **WHEN** a notification lists the current user's ID in `recipients.users` with wildcard `"Both"`
- **THEN** the notification is kept after filtering

#### Scenario: Already read notification
- **WHEN** a notification would otherwise match but the current user's ID is already in `readBy`
- **THEN** the notification is filtered out

### Requirement: Filtered notifications are persisted locally
The app SHALL persist the filtered notification list to local storage under the `NOTIFICATIONS` key in SharedPreferences, so that it survives app restarts without requiring a new sync.

#### Scenario: Persistence after sync
- **WHEN** a sync finishes with filtered notifications for this user
- **THEN** the `NOTIFICATIONS` entry in SharedPreferences contains the filtered list serialized as JSON

#### Scenario: The user leaves
- **WHEN** a logout or an account deletion completes, or the server session ends
- **THEN** the stored notifications SHALL be cleared, so the next user on the device never sees them; that user's own notifications arrive with their sync

### Requirement: Notifications are displayed when the app is entered and after a sync
The app SHALL show the cached unread notifications to the user when the authenticated area is entered and after a metadata sync has downloaded them, one dialog at a time. Entering the app and a finished sync mark the notifications pending; the first screen that shows them consumes the mark before rendering, so resuming a screen again does not show the same notification twice. A notification is unread until the user accepts it: only the accept action marks it read. Only the Home while it shows the program list, and the list a program opens into (events, tracked entities or data sets), SHALL show notification dialogs, and only with a logged-in session. No other screen shows them: not the splash, the login screen (which also serves the PIN unlock) or the sync progress screen, not the Home's other sections (settings, about, troubleshooting), and not forms or dashboards.

#### Scenario: Entering the app with an unread notification cached
- **WHEN** the app is started, or the user logs in, and there is an unread notification cached
- **THEN** the app shows it on the Home program list, or on the program screen when the Home opens its only program by itself

#### Scenario: Several notifications
- **WHEN** there are several unread notifications to show
- **THEN** the app shows one dialog at a time, and shows the next only once the current one has been accepted or dismissed

#### Scenario: Dialog dismissed without accepting
- **WHEN** the user dismisses the notification dialog without accepting it
- **THEN** it stays cached and unread, and is not offered again during the same entry; it reappears the next time the app is started or after the next metadata sync

#### Scenario: Resuming a screen
- **WHEN** a screen resumes and nothing marked the notifications pending since they were last shown
- **THEN** no dialog is shown

#### Scenario: Screens outside the program lists do not show notifications
- **WHEN** an unread notification is cached and the splash, the login screen, the sync progress screen, a form or a dashboard resumes, or any screen resumes without a logged-in session
- **THEN** no dialog is shown

#### Scenario: A sync finishes in settings
- **WHEN** the user runs a metadata sync from the Home's settings section and it brings an unread notification
- **THEN** no dialog is shown in settings, and the notification is shown when the Home returns to the program list

### Requirement: Notification content supports Markdown
The notification dialog SHALL render `content` as Markdown using Markwon, so that authors can include formatting, lists, and links.

#### Scenario: Markdown formatting is rendered
- **WHEN** a notification's `content` contains Markdown (headings, bold, links)
- **THEN** the dialog renders formatted text rather than raw markdown characters

### Requirement: Notification content is localized via translations map
Each notification carries a `translations: Map<String, String>?` keyed by language code. The app SHALL resolve the device locale via `Locale.getDefault().getLanguage()` and display the matching translation if present; otherwise it SHALL fall back to the default `content` field.

#### Scenario: Translation exists for device locale
- **WHEN** the device locale is `es` and `translations["es"]` is defined
- **THEN** the dialog shows the Spanish translation

#### Scenario: No translation for device locale
- **WHEN** the device locale is `fr` and `translations["fr"]` is absent
- **THEN** the dialog shows the default `content`

### Requirement: Marking a notification as read syncs back to the server
When the user dismisses the notification via the accept button, the app SHALL append a `ReadBy` entry containing the current user's ID, display name, and timestamp to the notification, and SHALL PUT the full updated notification list back to `dataStore/notifications/notifications`.

#### Scenario: User accepts a notification
- **WHEN** the user clicks OK / Accept on the notification dialog
- **THEN** the app appends a new `ReadBy` entry for the current user and sends a `PUT dataStore/notifications/notifications` with the full updated list

#### Scenario: PUT fails
- **WHEN** the network request to mark the notification as read fails
- **THEN** the cached notifications are left as they were, so the notification stays unread and can be retried; it is not offered again during the same entry, and the next sync SHALL reconcile the read status

### Requirement: Read notifications are not shown again
Once a notification has been marked as read by the current user, subsequent filtering passes SHALL exclude it, so the user never sees the same notification twice.

#### Scenario: Sync after acceptance
- **WHEN** the user has accepted a notification and a new metadata sync runs
- **THEN** that notification is filtered out and never re-stored in `NOTIFICATIONS`
