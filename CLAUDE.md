# WIDP Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the WIDP client.

- **Flavor:** `widp` (app ID: `com.eyeseetea.widp`)
- **Current version:** `3.3.0.1-widp-fork-1`
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **SDK fork:** EyeSeeTea/dhis2-android-sdk `1.13.0.1-eyeseetea-fork-1` (2FA + auth patches)

## Project structure

13 modules: `:app`, `:commons`, `:login`, `:form`, `:tracker`, `:aggregates`, `:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`, `:dhis2-mobile-program-rules`, `:commonskmm`, `:stock-usecase`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/widp/` — WIDP flavor-specific code and resources
- `app/src/widpDebug/`, `app/src/widpRelease/` — build-type overrides

## Build and test

```bash
./gradlew assembleWidpDebug          # build WIDP debug APK
./gradlew testDebugUnitTest          # run unit tests
./gradlew testWidpDebugUnitTest      # run WIDP-specific unit tests
./gradlew ktlintCheck                # code style
```

Java 17 required. Gradle 8.9.3 with parallel execution.

## Customizations

5 confirmed WIDP customizations. Each has an OpenSpec spec in `openspec/specs/`:

| # | Spec slug | Status | Risk |
|---|-----------|--------|------|
| 1 | `change-server-url` | active | medium |
| 2 | `image-upload-no-resize` | active | low |
| 3 | `notifications` | active | high |
| 4 | `two-factor-auth` | active (SDK dependency) | medium |
| 5 | `url-data-element` | broken (rendering lost in Compose migration) | medium |

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a future merge conflict. When implementing or fixing a customization, always look for a solution that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/widp/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]` where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects them).

**Automerge verification:** After resolving any conflicted file that contains a customization, run `git diff develop-eyeseetea -- path/to/file` and verify the diff contains ALL customization lines for that file — not just the ones that were in the conflict markers. Git automerge can silently drop customization code in non-conflicting hunks. Compare against `customization-files.md`. See `conflict-rules.md` for the full rule.

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/widp/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/widp/upgrade-3.3.1-strategy.md` — upgrade phases and status
- `eyeseetea-docs/upgrade/widp/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules

## Upgrade context

Upgrading from `3.3.0.1` to `3.3.1`. Strategy has 6 phases (A-F). Current status tracked in `upgrade-3.3.1-strategy.md`. Always use **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against baseline — three-dot misses deletions from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy, writing tests with the same shape, applying the same transformation across files), proactively suggest extracting it into:
- A **CLAUDE.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the extraction. Do not create the artifact — propose it and wait for approval.
