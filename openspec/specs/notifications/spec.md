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
- **THEN** the metadata sync SHALL complete without failing, and SHALL leave the previously stored notifications untouched

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

### Requirement: Notifications are displayed on activity resume
The app SHALL load persisted notifications and present them to the user on activity resume in the base activity of the authenticated area.

#### Scenario: Showing a pending notification
- **WHEN** an authenticated activity resumes and there is at least one pending notification
- **THEN** the app shows a Material AlertDialog displaying the notification content

#### Scenario: No pending notifications
- **WHEN** an authenticated activity resumes and there are no pending notifications
- **THEN** no dialog is shown

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
- **THEN** the app does not lose the local state — the next sync SHALL reconcile the read status

### Requirement: Read notifications are not shown again
Once a notification has been marked as read by the current user, subsequent filtering passes SHALL exclude it, so the user never sees the same notification twice.

#### Scenario: Sync after acceptance
- **WHEN** the user has accepted a notification and a new metadata sync runs
- **THEN** that notification is filtered out and never re-stored in `NOTIFICATIONS`
