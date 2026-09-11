# WIDP upgrade to 3.4.2 — working notes

Temporary working notes for one upgrade. Not stable documentation: reusable merge rules
belong in `eyeseetea-docs/upgrade/conflict-rules.md`, and the final customization
inventory belongs in `eyeseetea-docs/customizations/widp/customization-files.md`.

## Header

- Client: `widp`
- Target version: `3.4.2` (`3.4.2-widp-fork-1`)
- Base branch: `develop-eyeseetea` at `8e0200bcc` (`3.4.2-eyeseetea-fork-1`)
- Previous state: `develop-widp` at `3252c0681` (`3.3.1-widp-fork-1`)
- Merge base: `8a4866305`
- SDK fork: `EyeSeeTea/dhis2-android-sdk` `1.14.2-eyeseetea-fork-1` (from the baseline)
- Upgrade branch: `feature-widp/redo_3_4_2`
- Started on: `2026-09-11`
- Status: `in_progress`

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` already carries 3.4.2
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `yes`
- validation started: `no`

## Decisions

Filled in after the merge, before any file is edited. One row per conflicted file plus
every shared file that differs without having conflicted.

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|

## Open Questions

Decisions that need a person: anything that changes behavior the client sees, touches the
SDK, removes a capability, or departs from the baseline.

- none yet

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Shared drift still differing

Files that differ from `develop-eyeseetea` without a confirmed customization title. The
upgrade does not close while this section has unexplained entries.

- not yet analysed

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
