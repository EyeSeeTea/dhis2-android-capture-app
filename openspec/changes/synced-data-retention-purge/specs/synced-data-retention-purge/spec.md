# Synced Data Retention Purge

## Purpose

Lets the app periodically shrink its local database by removing
already-synced records that exceed the retention limits an administrator
has configured server-side, without requiring a full local data wipe or
account deletion, and without any user-configurable retention rule on the
device itself.

## ADDED Requirements

### Requirement: Scheduled retention purge
The system SHALL support running a retention purge on a periodic schedule,
independent of the data/metadata sync schedule.

#### Scenario: Scheduled purge runs on its configured interval
- **WHEN** the configured purge interval elapses
- **THEN** the system runs a retention purge without requiring user
  interaction

#### Scenario: Purge schedule is configurable
- **WHEN** the user changes the purge frequency in the sync settings screen
- **THEN** subsequent scheduled purges follow the newly selected frequency

#### Scenario: Purge disabled
- **WHEN** the user sets the purge frequency to manual/disabled
- **THEN** no scheduled purge runs until a frequency is selected again or
  the user manually triggers a purge

### Requirement: Manual on-demand purge
The system SHALL allow the user to trigger a retention purge on demand,
independent of the configured schedule.

#### Scenario: User triggers an immediate purge
- **WHEN** the user requests an immediate purge from the sync settings
  screen
- **THEN** the system runs a retention purge without waiting for the next
  scheduled occurrence

#### Scenario: Manual trigger while a purge is already running
- **WHEN** the user requests an immediate purge while a purge is already in
  progress
- **THEN** the system does not start a second, concurrent purge

### Requirement: Purge status visibility
The system SHALL show the user the status of the retention purge in the
sync settings screen.

#### Scenario: Purge in progress
- **WHEN** a retention purge is currently running
- **THEN** the sync settings screen indicates that a purge is in progress

#### Scenario: Last purge timestamp
- **WHEN** a retention purge completes, successfully or not
- **THEN** the sync settings screen shows the timestamp of that last purge
  attempt

#### Scenario: Last purge outcome
- **WHEN** a retention purge completes
- **THEN** the sync settings screen indicates whether that last attempt
  succeeded or failed

### Requirement: Retention rules are server-driven
The system SHALL NOT expose any device-side configuration of what data is
retained or purged (limits, scope, or per-data-type rules); it SHALL rely
entirely on the retention limits already configured and synced from the
server.

#### Scenario: No local override of retention limits
- **WHEN** a retention purge runs
- **THEN** the amount and selection of data removed is determined solely by
  the limits already synced from the server, with no device-side setting
  able to change that outcome

### Requirement: Per-fork enablement
The system SHALL allow each build flavor to enable or disable the
scheduled/manual retention purge capability independently, defaulting to
disabled when not explicitly enabled.

#### Scenario: Capability disabled by default
- **WHEN** a build flavor does not explicitly enable the retention purge
  capability
- **THEN** no purge schedule is started and no purge-related controls are
  shown to the user

#### Scenario: Capability enabled for a flavor
- **WHEN** a build flavor explicitly enables the retention purge capability
- **THEN** the purge schedule, manual trigger, and status controls become
  available to the user in that flavor's build
