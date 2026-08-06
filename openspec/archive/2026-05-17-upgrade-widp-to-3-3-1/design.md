## Context

The WIDP fork at `3.3.0.1-widp-fork-1` needs to absorb the `3.3.1-eyeseetea-fork-1` baseline from `develop-eyeseetea`. The two-dot diff shows ~566 files changed, of which ~89 are flavor-isolated (`app/src/widp/**`).

A complication: during Phase A (session 2), `develop-eyeseetea` was accidentally merged into this branch and then reverted with `git revert -m 1` (commit `7389d1043`). Git now considers develop-eyeseetea fully merged — the merge-base equals develop-eyeseetea HEAD. A standard `git merge develop-eyeseetea` would be a no-op.

All conflict resolution follows `eyeseetea-docs/upgrade/conflict-rules.md`.

## Goals / Non-Goals

**Goals:**
- Move WIDP to the 3.3.1 baseline with all 5 customizations preserved
- Fix the broken URL data element rendering (#5)
- Fix the SMS 2FA string typo (#4) if confirmed
- Remove PSI leftovers
- Update SDK fork reference and version strings
- Produce a clean branch that can be built as `widpDebug`

**Non-Goals:**
- New features or customizations beyond what exists
- Refactoring customization code to improve isolation (save for a future change)
- Updating the SDK fork itself — only the reference in `libs.versions.toml` changes
- Modifying `develop-eyeseetea` — all work stays on the WIDP branch

## Decisions

### Decision 1: Revert-the-revert strategy for the merge

**Choice:** `git revert 7389d1043` (revert the revert), then resolve conflicts.

**Why over alternatives:**
- *Alternative A: `git merge develop-eyeseetea`* — won't work. Git sees develop-eyeseetea as already merged. No new changes would come in.
- *Alternative B: rebase onto develop-eyeseetea* — would rewrite 20+ WIDP-specific commits and lose merge history. High risk, no benefit.
- *Alternative C: cherry-pick develop-eyeseetea commits* — there are hundreds of commits in the 3.3.1 upgrade. Impractical.

The revert-the-revert re-introduces all develop-eyeseetea content that was undone. Conflicts will appear where WIDP customizations overlap with what develop-eyeseetea changed. This is effectively the same conflict set as a normal merge would produce.

### Decision 2: Conflict classification per conflict-rules.md

Apply the four categories from `conflict-rules.md`:

| Category | Applies to | Action |
|----------|-----------|--------|
| `accept_ours` | `app/src/widp/**`, `app/src/widpDebug/**` | Keep WIDP version unchanged |
| `accept_theirs` | Pure formatting, import reorder, shared tests without WIDP logic | Take develop-eyeseetea version |
| `manual_reapply_on_theirs` | Shared files with confirmed customizations (server URL, notifications, 2FA, image resize, URL data element) | Start from develop-eyeseetea, reinsert minimum WIDP delta |
| `defer_after_build_verification` | Unclear cases, possible PSI leftovers | Tentatively take develop-eyeseetea, verify after build |

### Decision 3: SDK fork reference update

**Choice:** Update `libs.versions.toml` to `dhis2sdk = "1.13.1-eyeseetea-fork-2"` as defined in develop-eyeseetea.

The 2FA patches in the SDK fork must remain compatible. The SDK fork carries patches in:
- `LogInCall.kt` — 2FA authentication flow
- `LoginPayload.kt` — two-factor code field
- `D2ErrorCode.java` — custom error codes for 2FA states

These are in the SDK repo, not in this app repo. Compatibility is verified by building and running 2FA login.

### Decision 4: URL data element reimplementation approach

The old rendering used `onDescriptionClick()` → `ShowDescriptionLabelDialog` in the RecyclerView-based form. Both were removed in the Compose migration. The reimplementation needs to:
1. Find where the Compose form UI renders field description/info dialogs
2. Hook into that path to append the URL when `FieldUiModel.url` is non-null
3. Make the URL tappable (clickable link or explicit open-in-browser action)

This will be explored in detail during task execution — the exact Compose component needs to be identified in the 3.3.1 codebase after the merge.

### Decision 5: Two-pass conflict resolution

**Pass 1 (autonomous):** Resolve all `accept_ours` and `accept_theirs` files without developer review. These are mechanical and low-risk.

**Pass 2 (supervised):** Pause after pass 1. Present the `manual_reapply_on_theirs` and `defer` files for developer review before touching them. Each file gets an expected delta defined upfront per the conflict minimization rule.

## Risks / Trade-offs

- **[Revert-the-revert may produce unexpected conflicts]** → Mitigation: the conflict set is equivalent to a normal merge. The mandatory post-merge preclassification from conflict-rules.md catches surprises before any file is edited.
- **[SDK fork incompatibility with 1.13.1]** → Mitigation: build verification. If 2FA breaks, the SDK fork needs a patch — but that's out of scope for this change (flag and escalate).
- **[URL data element reimplementation depends on unknown Compose structure]** → Mitigation: explore the 3.3.1 form UI after merge. If the info dialog component doesn't exist or is radically different, defer to a follow-up change.
- **[PSI leftover removal may accidentally delete WIDP code]** → Mitigation: only remove files that have zero overlap with confirmed WIDP customizations. Verify each candidate against `customization-files.md`.
