# UNICEF TJK eLMIS Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the UNICEF Tajikistan eLMIS programme.

- **Flavor:** `unicefTjkElmis` (app ID: `org.unicef.tjk.elmis`)
- **Current version:** 3.3.1 (target version pending Daler Q#4 — server `GET /api/system/info`)
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **Working branch:** `develop-unicef-tjk-elmis` (currently identical to `develop-eyeseetea` HEAD `8a4866305`; carries no client-specific commits yet)
- **SDK fork:** none (UNICEF does not introduce an SDK fork dependency in PR 01; develop-eyeseetea no longer carries one)
- **Server:** `http://172.16.0.99:18081` (HTTP plain, VPN-only)

## Project structure

13 modules: `:app`, `:commons`, `:login`, `:form`, `:tracker`, `:aggregates`, `:ui-components`, `:compose-table`, `:dhis_android_analytics`, `:dhis2_android_maps`, `:dhis2-mobile-program-rules`, `:commonskmm`, `:stock-usecase`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/unicefTjkElmis/` — UNICEF TJK eLMIS flavor-specific code and resources
- `app/src/unicefTjkElmisDebug/`, `app/src/unicefTjkElmisRelease/` — build-type overrides

## Build and test

```bash
./gradlew assembleUnicefTjkElmisDebug          # build UNICEF TJK eLMIS debug APK
./gradlew testDebugUnitTest                    # run unit tests (cross-flavor)
./gradlew testUnicefTjkElmisDebugUnitTest      # run UNICEF TJK eLMIS-specific unit tests
./gradlew ktlintCheck                          # code style
```

Java 17 required. Gradle 8.9.3 with parallel execution.

## Customizations

None active yet. PR 01 (this baseline) is a flavor scaffold only — no functional customization is shipped. Functional capabilities are blocked on external inputs:

| # | Spec slug | Status | Risk | Blocker |
|---|-----------|--------|------|---------|
| – | (none yet) | – | – | – |

When a customization lands, add a row to the table above, create the corresponding `openspec/specs/<capability>/spec.md`, and update `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md`.

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a future merge conflict. When implementing or fixing a customization, always look for a solution that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/unicefTjkElmis/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]` where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects them). Place the comment **right above the customized block**, not above the containing scope.

**Automerge verification:** After any merge of the baseline, run `git diff develop-eyeseetea -- path/to/file` for **every file listed in `customization-files.md`** — not only files git marked as conflicted. Git automerge can silently apply baseline commits that delete customization wiring, dropping code with no conflict markers. Compare each diff against the inventory and recover missing lines before staging. The rule is load-bearing only if the inventory is complete: for each customization, cross-check `git show <feat-commit> --stat` against `customization-files.md` so no wiring file is missing. See `eyeseetea-docs/upgrade/conflict-rules.md` for the full rule.

**Post-merge check hierarchy:** marker-count < symbol-scan < diff-scan with semantic filter < manual emulator test. Each level catches what the previous one misses. Manual test in emulator is the irreplaceable last-line safety net — automated checks miss runtime rendering bugs and casualties in files without `// EyeSeeTea customization` markers.

## Network and security

The UNICEF TJK eLMIS server runs on plain HTTP behind a VPN. The flavor declares a scoped `network_security_config.xml` for cleartext access to `172.16.0.99` only:

- `app/src/unicefTjkElmis/AndroidManifest.xml` — references `@xml/network_security_config`
- `app/src/unicefTjkElmis/res/xml/network_security_config.xml` — `<domain-config cleartextTrafficPermitted="true">` scoped to `172.16.0.99`

The cleartext exception MUST stay flavor-scoped. Do not modify `app/src/main/AndroidManifest.xml` or other flavors. When the UNICEF server moves to TLS, remove the exception in a dedicated change proposal.

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does; empty in PR 01)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/unicefTjkElmis/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules

## Upgrade context

The fork is currently on `develop-eyeseetea` HEAD `8a4866305` (post-3.3.1 promotion of AI tooling and merge rules). The next baseline upgrade is unscoped — it lands when Oslo cuts the next release that develop-eyeseetea wants to absorb. Always use **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against baseline — three-dot misses deletions from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy, writing tests with the same shape, applying the same transformation across files), proactively suggest extracting it into:
- A **CLAUDE.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the extraction. Do not create the artifact — propose it and wait for approval.
