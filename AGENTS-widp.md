# WIDP Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the WIDP client.

- **Flavor:** `widp` (app ID: `com.eyeseetea.widp`)
- **Current version:** `3.4.2-widp-fork-1`
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **SDK fork:** EyeSeeTea/dhis2-android-sdk `1.14.2-eyeseetea-fork-3` (2FA + auth + disabled-account login patches)

## Project structure

14 modules: `:app`, `:commons`, `:login`, `:form`, `:tracker`, `:aggregates`, `:sync`,
`:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`,
`:dhis2-mobile-program-rules`, `:commonskmm`, `:stock-usecase`.

`:core` is **not** one of them — it belongs to the DHIS2 SDK and is only on the classpath
when `settings.gradle.kts` resolves a local SDK checkout as a composite build. See
`eyeseetea-docs/SDK_Setup.md`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/widp/` — WIDP flavor-specific code and resources
- `app/src/widpDebug/`, `app/src/widpRelease/` — build-type overrides

The branch also carries `app/src/eyeseetea/` (the baseline's own flavor, inherited through
the merge) and Oslo's `dhis2`, `dhis2PlayServices` and `dhis2Training`. No other client's
flavor belongs here.

## Build and test

```bash
./gradlew :app:assembleWidpDebug             # build the WIDP debug APK
./gradlew :app:testWidpDebugUnitTest         # WIDP unit tests
./gradlew :app:compileEyeseeteaDebugKotlin   # the other flavor on this branch — CI does not build it
./gradlew :login:allTests                    # login module (KMP), includes the 2FA tests
./gradlew ktlintCheck                        # code style
```

There is no `testDebugUnitTest` task: unit tests are per flavor.

Java 17 required. Gradle 9.0.1 (AGP), Kotlin 2.3.20.

## Customizations

5 confirmed WIDP customizations. Each has an OpenSpec spec in `openspec/specs/`:

| # | Spec slug | Status | Risk |
|---|-----------|--------|------|
| 1 | `change-server-url` | active | medium |
| 2 | `image-upload-no-resize` | active | low |
| 3 | `notifications` | active | high |
| 4 | `two-factor-auth` | active (SDK dependency) | medium |
| 5 | `url-data-element` | active | medium |

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a
future merge conflict. When implementing or fixing a customization, always look for a solution
that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in
the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the
default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/widp/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]`
where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects
them). Place the comment **right above the customized block**, not above the containing scope.

**Automerge verification:** After any merge of the baseline, run
`git diff develop-eyeseetea -- path/to/file` for **every file listed in
`customization-files.md`** — not only files git marked as conflicted. Git automerge can silently
apply baseline commits that delete customization wiring, dropping code with no conflict markers.
Compare each diff against the inventory and recover missing lines before staging. The rule is
load-bearing only if the inventory is complete: for each customization, cross-check
`git show <feat-commit> --stat` against `customization-files.md` so no wiring file is missing.
See `eyeseetea-docs/upgrade/conflict-rules.md` for the full rule.

**Post-merge check hierarchy:** marker-count < symbol-scan < diff-scan with semantic filter <
manual emulator test. Each level catches what the previous one misses. Manual test on a device is
the irreplaceable last-line safety net — automated checks miss runtime rendering bugs and
casualties in files without `// EyeSeeTea customization` markers.

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/widp/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/widp/upgrade-3.4.2-notes.md` — notes for the current upgrade
- `eyeseetea-docs/upgrade/widp/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules
- `eyeseetea-docs/customization-techniques.md` — reusable mechanisms for customizing Oslo code

## Upgrade model

Upgrades arrive by **merging `develop-eyeseetea`**, never by merging Oslo into this branch and
never by cherry-picking from another client's fork. Each upgrade is modelled as an OpenSpec
change under `openspec/changes/upgrade-widp-to-<version>/`, with the per-upgrade working notes in
`eyeseetea-docs/upgrade/widp/upgrade-<version>-notes.md`.

Always use a **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against the
baseline — three-dot misses deletions coming from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure
executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy,
writing tests with the same shape, applying the same transformation across files), proactively
suggest extracting it into:
- An **AGENTS-widp.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the
extraction. Do not create the artifact — propose it and wait for approval.
