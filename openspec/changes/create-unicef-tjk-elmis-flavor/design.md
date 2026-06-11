## Context

The fork branch `develop-unicef-tjk-elmis` is currently byte-identical to `develop-eyeseetea` HEAD `8a4866305`. The repository ships four flavors today: `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`. None of them target the UNICEF DHIS2 server and none are appropriate to repurpose — each one carries its own `applicationId`, signing config, and (for `eyeseetea`) launcher branding that conflicts with the UNICEF identity.

The UNICEF DHIS2 server lives at `http://172.16.0.99:18081`, on a private network reachable only via UNICEF VPN. There is no DNS, no TLS, and the deployment is not expected to acquire either in the short term — these are constraints on the host network, not preferences. Android targets API 28+ where cleartext HTTP is denied by default; the SDK fork (`com.github.EyeSeeTea:dhis2-android-sdk`) ships a `res/xml/network_security_configuration.xml` with `<base-config cleartextTrafficPermitted="true">` and references it from its `<application>` element, which becomes the effective network security policy for every flavor after manifest merging. The baseline `app/src/main/AndroidManifest.xml` also declares `android:usesCleartextTraffic="true"`, but per Android docs that attribute is ignored when `networkSecurityConfig` is set — so the SDK's config is what actually permits HTTP at runtime. UNICEF inherits this without any flavor-scoped configuration.

The existing `eyeseetea` flavor block in `app/build.gradle.kts` is the closest in-baseline reference for the shape of a client flavor: `applicationId`, `dimension = "default"`, `versionCode`/`versionName` from the version catalog, no signing config (release signing comes from environment variables, not declared per flavor). The matching `eyeseetea` block in `login/build.gradle.kts` declares the `LOGIN_TEST` `buildConfigField` — a constant the login module reads at compile time and which must be declared on every product flavor or the login module fails to compile.

## Goals / Non-Goals

**Goals:**
- A buildable `unicefTjkElmis` flavor variant that produces an APK installable on Android.
- Identity (`applicationId`, launcher name) that is stable for the lifetime of the published app — `applicationId` is immutable post-publication, so it must be settled now.
- HTTP access to the UNICEF server inherited from the baseline's global `usesCleartextTraffic="true"` — no flavor-scoped network configuration in this change.
- Build wiring that follows the existing `eyeseetea` flavor's shape, so a future fork can replicate the pattern without inventing.
- Zero modification to shared code (`app/src/main/`) and zero impact on other flavors.

**Non-Goals:**
- Final UNICEF / MoH TJK / combined launcher icon — the icons shipped here are a placeholder-stable UNICEF set (UNICEF logo on white) sourced from a single PNG; the dedicated branding change will replace them with the official asset bundle once the final logo source decision lands.
- Themed colors, typography, splash overrides — same deferral.
- Any functional customization (FRQ037 enforcement, photo proof, upload policy, roles matrix). Each lands as its own change once the corresponding external input is resolved.
- CI configuration for the new flavor — manual local build is the verification gate for this change.
- Signing config for release builds — the existing environment-variable-driven `signingConfigs.release` block is reused once Play Store deployment is on the table; not in scope here.
- Hardening the global `usesCleartextTraffic="true"` stance in `app/src/main/AndroidManifest.xml` and adding a per-host `network_security_config.xml` for UNICEF — that is a multi-flavor refactor (would force `dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea` to opt back in if they depend on cleartext) and belongs in its own change. See "Decision 4" for the rationale.

## Decisions

### Decision 1: One change, two artifact buckets

The change is split into two coherent buckets at apply time, each implementable as an independent commit and independently buildable: (1) Gradle declarations in `app/build.gradle.kts` and `login/build.gradle.kts`; (2) flavor source set with `strings.xml`, `ic_launcher_background.xml`, the launcher icon set, and the flavor-required Dagger module. Bucket 1 alone produces a buildable variant (using inherited resources). Bucket 2 layers on the UNICEF identity (launcher name and icon). HTTP access to the UNICEF server is inherited from the baseline's global `usesCleartextTraffic="true"`; no flavor manifest or `network_security_config.xml` is needed.

**Alternative considered:** Single squashed commit. Rejected — splitting makes review easier and lets a maintainer pinpoint regressions to a specific bucket if a future baseline merge breaks one of them.

### Decision 2: applicationId `org.unicef.tjk.elmis` (locked for life)

Decided once and only once — Play Store identifies the publication by `applicationId`, and changing it forces a new listing with no upgrade path for existing users. The `org.unicef.tjk` prefix aligns with the UNICEF Tajikistan publishing context; the `.elmis` suffix scopes to this programme so a future UNICEF Android app can sit under the same prefix without collision.

**Alternative considered:** `com.unicef.tjk.elmis`. Rejected — UNICEF is a non-profit organization, conventionally `org.*`.

### Decision 3: Flavor name `unicefTjkElmis` (camelCase, three parts spelled out)

Camel-case is required by Gradle for product flavors that drive source set paths (`app/src/<flavor>/`). The three-part spelling breaks the short-acronym pattern of `widp` / `eyeseetea` deliberately — there is no widely-known acronym for "UNICEF Tajikistan eLMIS", and abbreviating risks ambiguity with other UNICEF country deployments.

**Alternative considered:** `unicef`. Rejected — too generic; a future UNICEF deployment in another country would collide.

### Decision 4: No flavor-scoped network config in this change

The SDK fork already declares `<base-config cleartextTrafficPermitted="true">` in its bundled `network_security_configuration.xml`, and references it from `<application>` in its own manifest contribution. The merged manifest for the `unicefTjkElmis` flavor inherits that reference, so HTTP to `http://172.16.0.99:18081` succeeds without any flavor-scoped configuration. The build is functional with one less moving part. (The baseline `app/src/main/AndroidManifest.xml` also has `android:usesCleartextTraffic="true"`, but per Android docs that attribute is ignored when `networkSecurityConfig` is also present — so the cleartext allow comes from the SDK config in practice.)

A flavor-scoped `network_security_config.xml` (whitelist only `172.16.0.99`, deny cleartext to every other host) is technically tighter than the inherited global allow — but it is not free:
- it requires `tools:replace="android:networkSecurityConfig"` on the flavor `<application>` element to override the SDK fork's own `network_security_configuration` declaration, which is one more piece of merger ceremony to keep working through baseline upgrades;
- it does NOT prevent other flavors (`dhis2*`, `eyeseetea`) from making cleartext requests to arbitrary hosts — they still inherit the global allow;
- the security model UNICEF actually relies on is the VPN boundary, not the per-host whitelist on the device.

The hardened model — `usesCleartextTraffic="false"` globally, every flavor opts back in to the cleartext domains it actually needs — is the correct long-term shape for the entire repo, not for this PR. It is a multi-flavor refactor that affects `dhis2*` and `eyeseetea` and belongs in its own change proposal.

**Alternative considered:** Ship a flavor-scoped `network_security_config.xml` plus flavor `AndroidManifest.xml` with `tools:replace`. Rejected — provides marginal hardening only for `unicefTjkElmis` variants while the global cleartext stance for the repo stays unchanged; the `STPR_INVARIANTS §6` clause about "flavor-scoped, never global" is more precisely understood as "do not extend cleartext to other flavors" rather than "narrow it for this one"; and the cost (one manifest merger override to maintain) outweighs the benefit when the VPN already enforces the network boundary.

**Alternative considered:** Tighten the global stance now (`usesCleartextTraffic="false"` in `app/src/main/AndroidManifest.xml`, plus per-flavor network_security_configs for every flavor that needs cleartext). Rejected — out of scope for the bootstrap of a new flavor. Tracked separately as a candidate change.

### Decision 5: Ship a UNICEF placeholder icon set generated from a single source PNG

A UNICEF logo PNG is available, so the flavor ships a full launcher icon set rather than inheriting the Oslo default. The icons are programmatically generated from one trimmed source image, scaled with Lanczos resampling and saved as lossless WebP at every density expected by Android Studio's Image Asset wizard:

- `mipmap-mdpi/ic_launcher{,_round}.webp` (48×48), `ic_launcher_foreground.webp` (108×108)
- `mipmap-hdpi/...` (72×72 / 162×162)
- `mipmap-xhdpi/...` (96×96 / 216×216)
- `mipmap-xxhdpi/...` (144×144 / 324×324)
- `mipmap-xxxhdpi/...` (192×192 / 432×432)
- `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` (adaptive icon wrappers)
- `values/ic_launcher_background.xml` (`#FFFFFF` for both the main flavor and the debug build type)
- `ic_launcher-playstore.png` (512×512)

The adaptive foreground centers the logo inside the central 66% safe-zone square of the 108dp canvas, leaving transparent margin so the launcher's mask (circle, rounded-square, squircle) crops the canvas without cutting the icon mark.

**Debug build type override:** `app/src/unicefTjkElmisDebug/` carries the same icon set as the release variant. Debug and release installs are differentiated by application ID (`org.unicef.tjk.elmis.debug` vs `org.unicef.tjk.elmis`, via the debug build type's `applicationIdSuffix=".debug"`), which lets them coexist on the same device while sharing the same launcher visual. The `eyeseeteaDebug` source set uses a slightly different visual; UNICEF chooses identical visuals deliberately so a tester does not infer "different fork" from "different color".

Caveat: the `mipmap-*/ic_launcher.webp` and `mipmap-*/ic_launcher_round.webp` files under `app/src/unicefTjkElmisDebug/` were generated with a warm-tinted background baked into the pixels (a leftover from the initial generator pass). On Android API 26 and higher, those legacy webp files are not rendered — the adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` referencing `@color/ic_launcher_background = #FFFFFF`) takes precedence, so the launcher renders pure white. On pre-API 26 devices the legacy webp would render with the warm tint, which is a minor cosmetic divergence that is not worth a regeneration pass given the target device population is Android 8+ in practice.

**Alternative considered:** Inherit the Oslo default launcher icon from `app/src/main/res/mipmap-*/`. Rejected — UNICEF identity is already settled (name, applicationId, server) and a UNICEF logo is at hand; visually identifying the install as a DHIS2 default in a multi-DHIS2 device pool creates avoidable confusion for facility staff during testing.

**Alternative considered:** Ship a richer placeholder with co-branding scaffolding. Rejected for now — placement of UNICEF / MoH TJK relative to each other is exactly the open question the dedicated branding change resolves; preempting it would force a rework. Single-logo UNICEF is a stable interim because the UNICEF mark will appear on the icon regardless of which final variant is chosen.

### Decision 9: Debug-build-type strings.xml override

`app/src/debug/res/values/strings.xml` (Oslo's debug build-type source set, shared across all flavors) declares `app_name="Dhis2 Dev"`. Android resource merging applies build-type overrides AFTER flavor overrides, so without a flavor+buildType override the debug variant of `unicefTjkElmis` renders `Dhis2 Dev` on the launcher instead of `UNICEF TJK eLMIS`.

The flavor MUST therefore declare its three identity keys in `app/src/unicefTjkElmisDebug/res/values/strings.xml` so the highest-priority layer (flavor + buildType) wins resolution. Since the brand does not differ between debug and release for UNICEF, the values are identical to `app/src/unicefTjkElmis/res/values/strings.xml`. The same reasoning applies to every locale variant under `app/src/unicefTjkElmisDebug/res/values-<locale>/strings.xml`.

This was discovered at apply time when the user installed the debug APK on an emulator and saw `Dhis2 dev` on the launcher. Catching this in a unit test is impractical (resource resolution depends on installed system locale at runtime); the upgrade-validation-checklist's first-install-and-login flow is the correct gate.

### Decision 7: Locale-specific strings.xml overrides for the three identity keys

`app/src/main/res/values-<locale>/strings.xml` declares translated values for `app_name`, `logo_text`, and `logo_number` in 20 locales (ar, ckb, cs, es, es-rES, fr, id, km, lo, nb, nl, pt, ru, sv, uk, uz, uz-rUZ, vi, zh, zh-rCN). Android resource resolution picks the most specific match: when a Russian-locale device installs the `unicefTjkElmis` variant, the resolver finds `app/src/main/res/values-ru/strings.xml` first (locale match) and ignores the flavor's default-locale `app/src/unicefTjkElmis/res/values/strings.xml` (no locale match). Result: a Russian-locale phone would render the launcher as `Dhis2` instead of `UNICEF TJK eLMIS`.

The flavor MUST therefore mirror every locale where main overrides these three keys, with the UNICEF brand values. The brand does not translate, so all 20 locale files carry identical content. The same set of files lands under `app/src/unicefTjkElmisDebug/` so the debug variant is consistent across locales.

**Alternative considered:** Override only the most likely locales for facility staff (ru, tg). Rejected — Tajikistan facilities may have phones set to ar, fr, or zh depending on cross-border staff or shared devices; missing a locale silently leaks the upstream brand. Mirroring main's full set is the safe default and adds zero translation cost.

**Alternative considered:** Strip the brand strings from `app/src/main/res/values-<locale>/strings.xml` and let every flavor's default `values/strings.xml` win. Rejected — that touches Oslo files, generates merge conflicts on every upstream pull, and breaks the upstream `dhis2*` flavors that depend on the translated `Dhis2` strings.

### Decision 8: Branch-wide `vName` set to the UNICEF value, no per-flavor override

`gradle/libs.versions.toml` is the single source of truth for `versionName` across every flavor in the repo. On the `develop-unicef-tjk-elmis` branch, the only flavor that actually gets distributed is `unicefTjkElmis` — `dhis2`, `dhis2PlayServices`, `dhis2Training`, and `eyeseetea` exist on this branch for build coherence (so the same Gradle config compiles cleanly when this branch is merged into or from `develop-eyeseetea`), not for distribution. So setting the shared `vName` to the UNICEF value at the catalog level matches this branch's identity:

```toml
vName = "3.3.1-unicefTjkElmis-fork-1"
```

The `unicefTjkElmis` flavor block continues to read `versionName = libs.versions.vName.get()`, identical to the existing `eyeseetea` flavor block's shape. No per-flavor hard-coded version, no new catalog entry, no per-flavor catalog map — just a one-line change to the existing key. `versionCode` continues to come from `libs.versions.vCode`.

**Trade-off accepted:** an `eyeseeteaDebug` or `dhis2Debug` APK built from this branch (which would normally not happen in practice, since this branch does not distribute those flavors) would carry the UNICEF version string in its `versionName`. This is misleading-by-stripe but harmless because such builds are never published from this branch — `develop-eyeseetea` is the canonical baseline for `eyeseetea` distribution, and `develop-unicef-tjk-elmis` is for UNICEF only.

**Alternative considered:** Hard-code `versionName = "3.3.1-unicefTjkElmis-fork-1"` inside the `unicefTjkElmis` flavor block. Rejected by the maintainer — the value is per-branch, not per-flavor on this branch, and the catalog is the conventional place to keep version strings.

**Alternative considered:** Add a new catalog entry `vNameUnicefTjkElmis` and reference it only from the unicefTjkElmis flavor block. Rejected by the maintainer — the catalog already has the per-branch `vName` slot; introducing a parallel entry duplicates the convention without solving a real problem on a single-flavor-per-branch fork.

This decision is consistent with `STPR_INVARIANTS §4` ("`versionCode` and `versionName` come from `libs.versions.vCode` / `libs.versions.vName`. Do not hard-code per flavor unless an explicit plan justifies it"): the catalog stays the source of truth, and no flavor hard-codes its version.

### Decision 6: One Kotlin file under `app/src/unicefTjkElmis/java/` — flavor-required Dagger module

`app/src/unicefTjkElmis/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` MUST exist or `kspUnicefTjkElmisDebugKotlin` fails with `ComponentProcessingStep was unable to process 'org.dhis2.AppComponent' because 'GranularSyncModule' could not be resolved`. The Oslo Dagger graph references `GranularSyncModule` from shared code (`app/src/main/java/org/dhis2/utils/granularsync/SyncStatusDialog.kt`, `app/src/main/java/org/dhis2/utils/granularsync/GranularSyncComponent.kt`, `app/src/main/java/org/dhis2/data/server/ServerComponent.java`) but the module class itself lives only in flavor source sets — every flavor (`dhis2`, `dhis2PlayServices`, `dhis2Training`, `eyeseetea`) ships its own copy.

This is build-wiring boilerplate, not a customization with a SHALL/MUST contract: the file has no UNICEF-specific behavior, no `// EyeSeeTea customization` comment, and is not listed under shared-code customizations in `customization-files.md`. It is listed under §1.1 (flavor code) of the inventory because it is a flavor-scoped file that must remain in sync if the upstream Dagger graph changes.

The contents are copied verbatim from the `dhis2` flavor's version (`app/src/dhis2/java/org/dhis2/utils/granularsync/GranularSyncModule.kt`) — that file follows the upstream Oslo shape and uses a `provideDispatchers()` helper. The `eyeseetea` flavor's version has slightly different shape (inlines the DispatcherProvider) but produces an equivalent Dagger graph; choosing the `dhis2` shape keeps UNICEF closer to upstream when the Dagger graph next moves.

**Alternative considered:** Add the file under shared `app/src/main/java/` and remove the requirement for each flavor to ship it. Rejected — that would touch Oslo's flavor-coupling convention and leak `GranularSyncModule` resolution into shared code. The flavor-coupled placement is what every other flavor in this repo already does; respecting it costs one boilerplate file and keeps the conflict surface inside the flavor source set.

**Alternative considered:** Discover the requirement at first apply and silently create the file. Rejected — that would leave the change proposal misaligned with reality (proposal said "no Kotlin sources", reality has one). Updating the proposal/design/spec at the moment of discovery preserves the trail.

## Risks / Trade-offs

[Risk] **Future baseline merge from `develop-eyeseetea` removing the flavor block** — Git's automerge can silently drop the new `productFlavors` entry if `develop-eyeseetea` reorganizes the surrounding code. → Mitigation: the file `app/build.gradle.kts` is listed in `eyeseetea-docs/customizations/unicefTjkElmis/customization-files.md` §1.3 (build wiring) so the §"Automerge verification" rule from `eyeseetea-docs/upgrade/conflict-rules.md` runs `git diff develop-eyeseetea -- app/build.gradle.kts` and recovers the dropped block.

[Risk] **`org.unicef.tjk.elmis` collides with an existing UNICEF Android namespace** — if UNICEF already publishes an Android app under `org.unicef.tjk.*`, Play Store rejects the listing. → Mitigation: confirm with the UNICEF publishing account holder before the first Play Store upload. The applicationId is locked at this change but the *publication* is far downstream — there is room to adjust before any user-facing harm.

[Risk] **SDK fork tightens its `network_security_configuration.xml`** — if a future SDK fork release changes `<base-config cleartextTrafficPermitted="true">` to `false` (or scopes cleartext to a domain whitelist that excludes `172.16.0.99`), `unicefTjkElmis` login over plain HTTP breaks because no flavor-scoped allow exists. → Mitigation: the upgrade-validation-checklist's first-install-and-login flow exercises the HTTP login against the UNICEF VPN. Any SDK fork bump that breaks it surfaces a `CLEARTEXT communication ... not permitted` failure during validation, at which point the decision to ship a flavor-scoped network_security_config is forced and made deliberately.

[Risk] **Server moves to TLS or new IP** — if `172.16.0.99` is decommissioned or fronted by HTTPS, the URL stored on devices becomes stale. → Mitigation: there is no flavor-scoped config to update; the change is only at the user-facing server URL field.

[Trade-off] **Cleartext is allowed globally, not narrowed for UNICEF** — `unicefTjkElmis` variants can technically make cleartext requests to any host, not just `172.16.0.99`. This matches every other flavor's posture in this repo. → Trade-off accepted; the security boundary the deployment relies on is the UNICEF VPN, not a per-host whitelist on the device. A repo-wide hardening of the global cleartext stance is tracked as a separate candidate change.

[Trade-off] **Empty `app/src/unicefTjkElmis/java/` (apart from the Dagger module)** — no functional source code in this change means tooling that expects a populated source set may emit warnings. → Trade-off accepted; warnings are cosmetic and the first functional customization closes the gap.

## Migration Plan

There is no migration — this is a greenfield flavor on a clean branch. Rollback is `git revert` of the change's commits, which restores `develop-unicef-tjk-elmis` to `8a4866305` (= `develop-eyeseetea`). No persisted state is created on devices because the flavor has not been distributed.

## Open Questions

(none — every decision required for the apply step is settled. Final branding, server TLS migration, and functional customizations are tracked as separate changes.)

## References

- `eyeseetea-docs/upgrade/conflict-rules.md` — the §"Automerge verification" rule applies to the build files modified here once the change lands.
- `app/build.gradle.kts` `eyeseetea` flavor block — the in-baseline reference for the shape of a client flavor declaration.
- `login/build.gradle.kts` `eyeseetea` flavor block — same, for the login module.
