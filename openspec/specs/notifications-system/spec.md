# Notifications System

## Context

The sports fork includes a server-side notification system that allows administrators to push messages to app users. Notifications are stored in the DHIS2 dataStore (`dataStore/notifications/notifications`) and fetched via the SDK HTTP client. The system supports Markdown content rendering, locale-based translations, recipient targeting (by user, user group, or wildcard), and read-tracking.

Notifications are synced during the regular data synchronization flow and displayed in the main screen. Users can view and mark notifications as read.

This is a custom EyeSeeTea feature not present in the upstream DHIS2 Android Capture App.

## Requirements

### REQ-NS-01: Fetch notifications on sync

The app MUST fetch notifications from the DHIS2 dataStore endpoint (`dataStore/notifications/notifications`) during each synchronization cycle.

The sync operation MUST NOT block the overall sync flow if the notifications endpoint fails -- errors SHALL be logged and the sync SHALL continue.

### REQ-NS-02: Filter notifications for current user

After fetching all notifications, the app MUST filter them to show only notifications relevant to the current user based on:
- **Wildcard recipients**: notifications with `wildcard` set to `"ALL"` (case-insensitive) SHALL be shown to all users
- **User group recipients**: notifications targeting user groups that the current user belongs to SHALL be shown, provided the `wildcard` field indicates Android compatibility (`"Android"`, `"BOTH"`, or empty string, all case-insensitive)
- **Direct user recipients**: notifications targeting the current user by UID SHALL be shown, provided the `wildcard` field indicates Android compatibility

Notifications already read by the current user (present in `readBy` list with matching user UID) MUST be excluded.

### REQ-NS-03: Cache notifications locally

Filtered user notifications MUST be persisted locally using the preference provider (key: `NOTIFICATIONS`) so they are available without network access.

The local cache MUST be updated on every successful sync.

### REQ-NS-04: Render Markdown content

Notification content MUST support Markdown formatting. The presentation layer MUST render Markdown content appropriately when displaying notifications to the user.

### REQ-NS-05: Support locale-based translations

Notifications MAY include a `translations` map keyed by locale code. When a translation is available for the device locale, the app SHOULD display the translated content instead of the default `content` field.

### REQ-NS-06: Display notifications in main screen

The main screen MUST provide a way to access and view unread notifications. After sync completes, any pending notifications SHALL be presented to the user.

### REQ-NS-07: Mark notifications as read

The app MUST allow users to mark individual notifications as read. Marking a notification as read MUST update the notification on the server via the dataStore API (PUT to `dataStore/notifications/notifications`) by adding the current user to the `readBy` list.

### REQ-NS-08: Notification domain model

The `Notification` domain entity MUST include: `id`, `content`, `createdAt`, `readBy` list, `recipients` (user groups, users, wildcard), optional `permissions`, and optional `translations` map.

### REQ-NS-09: Repository interface in domain layer

The `NotificationRepository` interface MUST be defined in the domain layer (`usescases/notifications/domain/`) with the following operations:
- `sync(): Flow<Unit>` -- fetch and cache notifications from server
- `get(): Flow<List<Notification>>` -- retrieve cached notifications
- `getById(id: String): Flow<Notification?>` -- retrieve a single notification
- `save(notification: Notification): Flow<Unit>` -- update a notification on the server

## Scenarios

### Scenario: Notifications fetched during sync

- **GIVEN** the app is performing a data synchronization
- **WHEN** the sync flow reaches the notification sync step
- **THEN** notifications are fetched from `dataStore/notifications/notifications`
- **AND** filtered for the current user
- **AND** cached locally

### Scenario: Notifications sync failure does not block sync

- **GIVEN** the notifications dataStore endpoint returns an error (e.g., 404, 500, network timeout)
- **WHEN** the sync flow reaches the notification sync step
- **THEN** the error is logged
- **AND** the overall synchronization continues without failure

### Scenario: User sees only their unread notifications

- **GIVEN** the server has 5 notifications: 2 targeting the current user's group, 1 targeting a different group, 1 targeting the user directly but already read, 1 with wildcard "ALL"
- **WHEN** notifications are fetched and filtered
- **THEN** 3 notifications are shown (2 from user group + 1 from wildcard)
- **AND** the already-read notification is excluded
- **AND** the notification for the other group is excluded

### Scenario: Notification displayed with Markdown

- **GIVEN** a notification has content with Markdown formatting (e.g., `**bold text**`, `[link](url)`)
- **WHEN** the notification is displayed to the user
- **THEN** the Markdown is rendered as formatted text

### Scenario: Notification displayed in user locale

- **GIVEN** a notification has a `translations` map with a key matching the device locale
- **WHEN** the notification is displayed
- **THEN** the translated content is shown instead of the default `content` field

### Scenario: User marks notification as read

- **GIVEN** the user views a notification
- **WHEN** the user marks it as read
- **THEN** the notification is updated on the server with the current user added to `readBy`
- **AND** the notification no longer appears in the unread notifications list

### Scenario: Notifications available offline from cache

- **GIVEN** the app has previously synced notifications
- **AND** the device is now offline
- **WHEN** the user opens the notifications view
- **THEN** the cached notifications are displayed
