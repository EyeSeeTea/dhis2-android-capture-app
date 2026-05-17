## Why

The WIDP fork is currently at `3.3.0.1-widp-fork-1` while the shared EyeSeeTea baseline (`develop-eyeseetea`) has already moved to `3.3.1-eyeseetea-fork-1`. This upgrade brings WIDP in line with the latest upstream Oslo release integrated by EyeSeeTea, picking up bug fixes, Compose migration progress, SDK improvements, and dependency updates. Delaying increases the conflict surface for future upgrades.

## What Changes

- **Rebase WIDP onto the 3.3.1 baseline** by re-introducing the `develop-eyeseetea` merge that was previously reverted (commit `7389d1043`). This is a revert-the-revert operation — git considers develop-eyeseetea already merged due to the accidental merge + revert in Phase A.
- **Preserve all 5 active WIDP customizations** through conflict resolution, reapplying minimum client-specific logic on top of the new baseline.
- **Fix broken URL data element rendering** (#5) — the data plumbing survived but the display was lost in the upstream Compose migration. Needs reimplementation in the current Compose UI.
- **Fix SMS 2FA string typo** (#4) — verify and correct "Email with two factor code sent" → "SMS with two factor code sent".
- **Clean up PSI leftovers** — remove files from a previous PSI fork that are not part of any active WIDP customization.
- **Update SDK fork reference** from `1.13.0.1-eyeseetea-fork-1` to `1.13.1-eyeseetea-fork-2` (as defined in develop-eyeseetea).
- **Update version** from `3.3.0.1-widp-fork-1` to `3.3.1-widp-fork-1`.

## Capabilities

### New Capabilities

None. This is a baseline upgrade, not a feature addition.

### Modified Capabilities

- `url-data-element`: Reimplement the rendering lost during upstream Compose migration. The requirement (show URL from data element in description dialog) is unchanged, but the implementation must be rebuilt on the new Compose-based form UI.

## Impact

- **~566 files** differ between the current branch and `develop-eyeseetea` (two-dot diff). After the revert-the-revert, most will be resolved automatically by git. Conflicts will concentrate in files where WIDP customizations touch shared code.
- **Conflict-prone areas** (from `conflict-rules.md`): form rendering, login flow (2FA), sync integration (notifications), settings/menu (server URL change).
- **Flavor-isolated files** (`app/src/widp/**`, `app/src/widpDebug/**`): ~89 files, all `accept_ours` — zero conflict risk.
- **SDK dependency**: moves from `1.13.0.1-eyeseetea-fork-1` to `1.13.1-eyeseetea-fork-2`. The 2FA patches in the SDK fork must remain compatible.
- **Build tooling**: Gradle `8.9.3` → `8.13.2`, Kotlin `2.2.20` → `2.2.21`, compile SDK 36 (unchanged). Build scripts and CI may need minor adjustments.

## Closure Notes

- The WIDP `3.3.1` upgrade is considered functionally complete and manually validated.
- Documentation alignment and upgrade consistency checks are complete.
- Remaining `androidTest` instability comes from the legacy `3.3.x` `login` test wiring and is intentionally deferred to the `3.4` upgrade path rather than being solved in this change.
