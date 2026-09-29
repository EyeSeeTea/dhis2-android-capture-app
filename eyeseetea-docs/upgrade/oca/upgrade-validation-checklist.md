# OCA Validation Checklist

Manual validation checklist for the known OCA customizations after an upgrade.

Use this file for:
- minimal manual test flows
- expected results
- identifying regressions after merge resolution

Do not use this file for:
- merge progress
- raw diff inventory
- implementation notes

## Status

1 confirmed OCA-specific functional customization (see `eyeseetea-docs/customizations/oca/customization-files.md`). Add one `## N. [Customization title]` section here, matching the title in `openspec/specs/<capability>/spec.md`, whenever a new OCA customization is confirmed.

## 1. Synced Data Retention Purge

Spec: `openspec/specs/synced-data-retention-purge/spec.md`.

- **Schedule change**: open Settings → Data retention purge, change the period (e.g. Every 24 hours → Every hour). Confirm the app schedules a new periodic purge at the new interval and cancels the previous one (no duplicate runs).
- **Manual trigger**: tap "Purge Now". Confirm a one-time purge starts immediately, independent of the configured period, and does not require a data/metadata sync to also be running.
- **Status visibility**: after a purge completes, confirm the "Last purge on" date updates on the Settings screen. Force a failure (e.g. no connectivity, or an invalid session) and confirm the screen shows a purge-failed state instead of a success date rollback.
- **Capability enabled on this fork**: confirm the "Data retention purge" row is visible on the Settings screen (`RetentionPurgeCapability.IS_ENABLED = true` on OCA — unlike the generic baseline, which defaults it off).

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `eyeseetea-docs/customizations/oca/customization-files.md`
