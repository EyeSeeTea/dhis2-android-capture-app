# OCA Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the OCA client.

- **Flavor:** `oca` (app ID: `oca.com.dhis2`)
- **Current version:** `3.4.2-oca-fork-1`
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **SDK fork:** EyeSeeTea/dhis2-android-sdk `1.14.2-eyeseetea-fork-1` (JitPack; no OCA-specific patches yet — same fork used by `develop-eyeseetea`)
- **Distribution:** published to Google Play. Release build/upload is a manual process run by the project's PM, not automated in this repo's CI.

## Project structure

13 modules: `:app`, `:commons`, `:login`, `:form`, `:tracker`, `:aggregates`, `:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`, `:dhis2-mobile-program-rules`, `:commonskmm`, `:stock-usecase`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/oca/` — OCA flavor-specific code and resources
- `app/src/ocaDebug/` — build-type overrides

## Build and test

```bash
./gradlew assembleOcaDebug          # build OCA debug APK
./gradlew testDebugUnitTest         # run unit tests
./gradlew testOcaDebugUnitTest      # run OCA-specific unit tests
./gradlew ktlintCheck               # code style
```

Java 17 required. Gradle 8.9.3 with parallel execution.

## Customizations

0 confirmed OCA-specific customizations as of this writing. The `oca` flavor exists (product flavor, `applicationId`, branding, and the boilerplate DI/extension-point files every flavor must carry — `PostMetadataSyncModule.kt`, `GranularSyncModule.kt`), but nothing in it diverges functionally from `develop-eyeseetea` yet. See `eyeseetea-docs/customizations/oca/customization-files.md` §1 for the flavor surface inventory.

When a real business customization is confirmed for OCA, add a row here and create its spec under `openspec/specs/<capability>/spec.md`:

| # | Spec slug | Status | Risk |
|---|-----------|--------|------|
<!-- add one row per active customization; status is one of: active, broken, deprecated -->

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a future merge conflict. When implementing or fixing a customization, always look for a solution that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/oca/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]` where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects them). Place the comment **right above the customized block**, not above the containing scope.

**Automerge verification:** After any merge of the baseline, run `git diff develop-eyeseetea -- path/to/file` for **every file listed in `customization-files.md`** — not only files git marked as conflicted. Git automerge can silently apply baseline commits that delete customization wiring, dropping code with no conflict markers. Compare each diff against the inventory and recover missing lines before staging. The rule is load-bearing only if the inventory is complete: for each customization, cross-check `git show <feat-commit> --stat` against `customization-files.md` so no wiring file is missing. See `eyeseetea-docs/upgrade/conflict-rules.md` for the full rule.

**Post-merge check hierarchy:** marker-count < symbol-scan < diff-scan with semantic filter < manual emulator test. Each level catches what the previous one misses. Manual test in emulator is the irreplaceable last-line safety net — automated checks miss runtime rendering bugs and casualties in files without `// EyeSeeTea customization` markers.

**Distribution-driven defaults:** OCA is published to Google Play, same as `eyeseetea`. Any file that behaves differently for Play-distributed vs. side-loaded flavors (e.g. `DownloadNewVersion.kt`) must use the `dhis2PlayServices` pattern, not `dhis2`'s — see `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` §1.1 for the criterion. This is baseline behavior, not an OCA customization.

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/oca/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/oca/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules

## Upgrade context

No upgrade in progress. `feature-oca/new_fork` is up to date with `develop-eyeseetea` (fork creation branch, not yet merged to a stable OCA baseline). When starting a future upgrade, always use **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against baseline — three-dot misses deletions from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy, writing tests with the same shape, applying the same transformation across files), proactively suggest extracting it into:
- An **AGENTS-oca.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the extraction. Do not create the artifact — propose it and wait for approval.
