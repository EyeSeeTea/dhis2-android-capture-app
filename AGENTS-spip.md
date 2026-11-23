# SP IP Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the SP IP client.

- **Flavor:** `spIP` (app ID: `org.samaritan.platform`)
- **Current version:** `3.3.1-spIP-fork-3`
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **SDK fork:** EyeSeeTea/dhis2-android-sdk `1.13.1-eyeseetea-fork-4` (image download backport for DHIS2 2.42)

## Project structure

13 modules: `:app`, `:commons`, `:login`, `:form`, `:tracker`, `:aggregates`, `:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`, `:dhis2-mobile-program-rules`, `:commonskmm`, `:stock-usecase`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/spIP/` — SP IP flavor-specific code and resources
- `app/src/spIPDebug/`, `app/src/spIPRelease/` — build-type overrides

## Build and test

```bash
./gradlew assembleSpIPDebug          # build SP IP debug APK
./gradlew testDebugUnitTest                            # run unit tests
./gradlew testSpIPDebugUnitTest      # run SP IP-specific unit tests
./gradlew ktlintCheck                                  # code style
```

Java 17 required. Gradle 8.9.3 with parallel execution.

## Customizations

Only customizations made going forward are documented as OpenSpec specs. The existing ones (Send Welcome SMS, image sync, download limit, etc.) are not inventoried yet: see the Pending item in `eyeseetea-docs/customizations/spip/customization-files.md`.

| # | Spec slug | Status | Risk |
|---|-----------|--------|------|
| 1 | `print-receipt` | planned | low |
<!-- add one row per customization as it is documented; status is one of: planned, active, broken, deprecated -->

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a future merge conflict. When implementing or fixing a customization, always look for a solution that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/spIP/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]` where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects them). Place the comment **right above the customized block**, not above the containing scope.

**Automerge verification:** After any merge of the baseline, run `git diff develop-eyeseetea -- path/to/file` for **every file listed in `customization-files.md`** — not only files git marked as conflicted. Git automerge can silently apply baseline commits that delete customization wiring, dropping code with no conflict markers. Compare each diff against the inventory and recover missing lines before staging. The rule is load-bearing only if the inventory is complete: for each customization, cross-check `git show <feat-commit> --stat` against `customization-files.md` so no wiring file is missing. See `eyeseetea-docs/upgrade/conflict-rules.md` for the full rule.

**Post-merge check hierarchy:** marker-count < symbol-scan < diff-scan with semantic filter < manual emulator test. Each level catches what the previous one misses. Manual test in emulator is the irreplaceable last-line safety net — automated checks miss runtime rendering bugs and casualties in files without `// EyeSeeTea customization` markers.

## Development workflow

Applies to customization work driven by an OpenSpec `tasks.md`. Execute the tasks by
hand, one at a time; do not use `/opsx:apply`, so the design checkpoint is not skipped.

**1. Design checkpoint (the user decides, before writing code).**
List the behaviors to build, the boundaries involved, and the level each test runs at.
Wait for approval.

**2. Red → green → refactor per behavior, without pausing between steps.**
- Write the test first and run it. It only counts as red if it fails for the expected
  reason (an assertion, not a compilation error). If a stub is needed to compile, say so
  in the summary.
- Write the minimum code that makes it green. Build only what the current test reads.
- Stop and ask only if: the red fails for another reason; a test passes without new
  code; a design decision outside the approved plan appears; or more files are touched
  than planned.

**3. Tests check observable behavior.**
- Assert on observable state or output (return values, emitted state, shown messages),
  not on which collaborator was called. Do not use `verify()` on interactions.
- Test at the level of the pillar that owns the behavior: the host
  (`ReceiptPrintHostTest`) or the adapter (`BluetoothReceiptPrinterTest`). No unit tests
  of ViewModels or other internal parts.
- Doubles only at the outer boundary (our own ports such as `PrinterTransport`,
  `BluetoothPermission`, `ReceiptRepository`, or the SDK). Pick the kind by its role:
  - A **fake** when the test observes what the double received or stored (the text sent
    to the printer, the saved receipt): assert on its state.
  - A **stub** with the mocking library (`mockito-kotlin`: `mock` + `whenever`) when the
    double only supplies data or a canned answer and the test does not observe it.
- Name each test after the contract it verifies, not the mechanism.
- Details of the Android framework or of external SDKs (permission registries,
  Bluetooth, the Brother SDK) stay behind a port of ours. Tests never touch them; the
  real adapter stays thin and is verified by hand on the emulator or the printer.

**4. Commits.**
- One commit per behavior, with a one-line message and no body.
- Propose the commits together at the end of the unit of work and wait. Never commit
  without the user's explicit authorization in that moment.
- Before proposing, run the reviews required by the user's global instructions. Skip them
  for trivial or docs-only commits and say so.
- Run `./gradlew testSpIPDebugUnitTest` before proposing. If the host test changed, also
  run it on the emulator (`connectedSpIPDebugAndroidTest`, class `ReceiptPrintHostTest`).

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/spip/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/spip/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules

## Upgrade context

No upgrade is scheduled for this fork (currently `3.3.1-spIP-fork-3`; `develop-eyeseetea` is ahead). Do not merge `develop-eyeseetea` outside of an explicit upgrade change. When the upgrade starts, follow `eyeseetea-docs/onboarding-fork-guide.md` (Phase 3 inventories the existing customizations) and always use **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against baseline — three-dot misses deletions from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy, writing tests with the same shape, applying the same transformation across files), proactively suggest extracting it into:
- An **AGENTS-spip.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the extraction. Do not create the artifact — propose it and wait for approval.
