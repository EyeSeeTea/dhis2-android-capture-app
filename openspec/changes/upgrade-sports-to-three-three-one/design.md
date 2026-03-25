## Context

The `develop-sports` branch has 6 unique commits on top of a shared ancestor with `develop-eyeseetea`. Meanwhile, `develop-eyeseetea` has 83 commits ahead — the upstream v3.3.1 release plus flavor cleanup and documentation infrastructure.

The sports branch **lacks `eyeseetea-docs/`**, which means it has no customization inventory, no conflict rules, and no validation checklist. Per the [onboarding fork guide](https://github.com/EyeSeeTea/dhis2-android-capture-app/blob/develop-eyeseetea/eyeseetea-docs/onboarding-fork-guide.md), this makes it an "existing fork without documentation" — requiring the full 8-phase onboarding before the merge.

**Key finding from diff analysis:**
- 63 flavor-specific files in `app/src/sports/` (icons, strings, flavor code) — these are `accept_ours`
- ~30 files in shared code that exist in sports but were **removed** by eyeseetea (notifications, ChangeServerURL, BasicPreference, old layouts) — these are almost certainly `accept_theirs` (removals)
- ~100 files differ due to upstream 3.3.1 evolution — mostly `accept_theirs` or `manual_reapply_on_theirs`
- The actual sports customization surface in shared code appears very small

## Goals / Non-Goals

**Goals:**
- Complete all 8 phases of the onboarding fork guide for the sports branch
- Document sports customizations before merging, so nothing is lost silently
- Bring `develop-sports` to the v3.3.1 baseline via `develop-eyeseetea`
- Retain only `sports` and `eyeseetea` flavors (plus base `dhis2` variants)
- Follow `conflict-rules.md` classification and resolution process
- Leave the branch in a state where future upgrades follow the documented process

**Non-Goals:**
- Refactoring sports code beyond conflict resolution needs
- Adding new sports features
- Merging Oslo directly into the sports branch (must go through `develop-eyeseetea`)
- Updating the SDK beyond what's in develop-eyeseetea

## Decisions

### 1. Follow the 8-phase onboarding guide

Rather than jumping to merge, follow Jorge's guide sequentially. Phases 1-3 (docs) are prerequisites. Phase 4-5 (tooling) are partially done. Phase 6 (merge) uses the conflict-rules.md framework. Phases 7-8 (tests + cleanup) finalize.

**Alternative considered**: Merge first, document later. Rejected because the guide explicitly requires documentation before upgrade — this prevents silent loss of customizations and enables systematic conflict classification.

### 2. Merge strategy: `git merge` on a feature branch

Create `feature-sports/upgrade-to-three-three-one` from `develop-sports`, merge `develop-eyeseetea` into it. This preserves full history and enables PR review.

**Alternative considered**: Rebase. Rejected — rewrites history, harder to trace sports-specific changes.

### 3. Conflict classification before resolution

Per `conflict-rules.md`, immediately after merge, classify every conflicted file into a preclassification table (`upgrade-<version>-notes.md`) before editing anything:

| Category | Expected files |
|----------|---------------|
| `accept_ours` | `app/src/sports/**`, `app/src/sportsDebug/**` |
| `accept_theirs` | Removed code (notifications, ChangeServerURL, BasicPreference), pure upstream evolution, formatting |
| `manual_reapply_on_theirs` | Shared files where sports added real business logic (likely very few) |
| `defer_after_build_verification` | Files where it's unclear if sports customization is still needed |

### 4. Resolve easy batch first, pause, then manual

Per the upgrade plan: resolve `accept_ours` and `accept_theirs` automatically, then **stop and present results** before touching manual conflicts. This is the key checkpoint to avoid silent loss.

### 5. Comment convention for surviving customizations

Any sports customization surviving in shared code must use:
```kotlin
// EyeSeeTea customization - [title]
```
With titles matching `customization-specs.md`. Per `conflict-rules.md`, prefer isolating custom helpers near end of file when feasible.

### 6. Java-to-Kotlin migration awareness

Per the guide's file migration rule: if a sports customization was in a `.java` file but `develop-eyeseetea` now uses a `.kt` replacement, reimplement in the Kotlin file, don't keep the old Java conflict.

## Risks / Trade-offs

- **[Risk] Sports customizations in shared code are actually drift, not intentional** → Mitigation: Phase 3 inventory with developer confirmation before merge. Classify uncertain diffs as `needs_validation`.
- **[Risk] Koin initialization fix conflicts with eyeseetea DI state** → Mitigation: Verify `0e0e9a305` is still valid. Sports has a `GranularSyncModule.kt` in the flavor source set — check if it's still needed.
- **[Risk] Removed code (notifications, ChangeServerURL) was actually needed by sports** → Mitigation: Inventory phase will confirm. If needed, it would go in `app/src/sports/` not shared code.
- **[Risk] 611-file diff makes manual review impractical** → Mitigation: Preclassification table reduces manual review to only the `manual_reapply_on_theirs` files, which appear to be a small subset.

## Open Questions

- Which sports customizations in shared code are intentional vs. inherited drift from the WIDP branch (sports was branched from a state that included WIDP/PSI code)?
- Does the sports flavor use Google Play Services? (`google-services.json` was removed in develop-eyeseetea)
- Should the `app/src/sports/java/` flavor code (`CustomizableConstants.kt`, `UserComponentFlavor.kt`, `eventCaptureRepositoryFunctions.kt`, `GranularSyncModule.kt`) be preserved as-is or adapted to the new eyeseetea baseline?
