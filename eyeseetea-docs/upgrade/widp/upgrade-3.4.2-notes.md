# WIDP Upgrade Notes — 3.4.2

Temporary working notes for the WIDP upgrade to 3.4.2. Conflict decisions, open questions and
follow-up checks live here until the upgrade closes.

Stable rules belong in `conflict-rules.md` (baseline-owned — do not edit from this branch).
The final customization inventory belongs in `customizations/widp/customization-files.md`.

## Header

- Client: `widp`
- Target version: `3.4.2-eyeseetea-fork-1` → `3.4.2-widp-fork-1`
- Base branch: `develop-eyeseetea` @ `f87bec8c3`
- Merge-base with baseline: `8a4866305`
- SDK: `1.13.1-eyeseetea-fork-3` → `1.14.2-eyeseetea-fork-1`
- Upgrade branch: `feature-widp/bring_last_changes_3_4_2`
- OpenSpec change: `openspec/changes/upgrade-widp-to-3-4-2/`
- Started on: `2026-09-02`
- Status: `in_progress`

## Why this is the second attempt

An earlier attempt merged Oslo (`origin/upstream/3.4.1`) **directly** instead of going through
`develop-eyeseetea`, and was redone. That branch is preserved locally.

Root cause: the local agent instructions in `.claude/CLAUDE.md` stated *"Never merge
`develop-eyeseetea` into a client branch… upgrades are replicated"*, which contradicts
`upgrade-plan-client-forks.md` Phase 2 and `conflict-rules.md` step 4. The rule conflated
*another client's fork* (never cherry-pick — correct) with *the shared baseline* (always merge).
Corrected on 2026-09-02.

Three defects traced to that one decision, all silent — no conflict, no compile error, no
failing test:

| Defect | Detail |
|---|---|
| Build tuning lost | `gradle.properties` reverted to `-Xmx4096M`; the fork's `-Xmx8g -XX:MaxMetaspaceSize=1g` was dropped. Merge-base, `develop-widp` and `develop-eyeseetea` all carry the same value, so **via the baseline there is no conflict at all** |
| `eyeseetea` flavor broken | `:app:kspEyeseeteaDebugKotlin FAILED` — `GranularSyncModule.kt` stayed pre-3.4.1 at the malformed path `app/src/eyeseetea/java/org.dhis2.utils/` |
| Shared fork docs stale | including `conflict-rules.md` itself, whose missing step 4 is what would have prevented the wrong merge |

Plus the notifications download was hand-rolled onto the **data** sync
(`MainViewModel.onDataSuccess()`) instead of the baseline's `PostMetadataSyncAction`, which
silently violated the `notifications` spec's own wording ("during metadata sync").

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` is already at 3.4.2
- OpenSpec change created: `yes`
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `n/a`
- validation started: `no`

## Conflict surface

Measured from merge-base `8a4866305`:

```
baseline changes:   1227 files
develop-widp:        177 files
overlap:              37 files   ← the only place conflicts can arise
```

Classification is in `openspec/changes/upgrade-widp-to-3-4-2/design.md`, decision D3.

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| _to be filled during task 2.4_ | | | | | |

## Open Questions

- Image upload without resizing: no written requirement exists anywhere, and only 1 of the 3
  original commits ever reached WIDP. Confirm with the client whether the behavior is still
  wanted before spending effort preserving it.
- SDK `LogInCall.generate2FAErrorIfRequired()` returns `null` for
  `REQUIRES_TWO_FACTOR_ENROLMENT`, so a user required to enrol in 2FA crashes instead of
  seeing a message. Unchanged in `1.14.2`. Out of scope here — needs an SDK-fork decision.
- `.github/workflows/eyeseetea-main.yml` runs `:app:testWidpDebugUnitTest`, which covers 1 of
  13 modules and never runs the 2FA tests in `:login`. Worth fixing, but not in this change.

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no` — baseline-owned, propose separately
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
