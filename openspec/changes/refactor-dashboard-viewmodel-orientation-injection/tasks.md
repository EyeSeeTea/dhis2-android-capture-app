## 1. Abstraction [SPORTS]

- [ ] 1.1 [BE] Add `OrientationProvider` interface in `commons/src/main/java/org/dhis2/commons/orientation/OrientationProvider.kt` with `fun isPortrait(): Boolean`
- [ ] 1.2 [BE] Add production impl (`SystemOrientationProvider` or similar) backed by `Resources.getSystem().configuration.orientation`
- [ ] 1.3 [BE] Wire `OrientationProvider` into the DI graph that constructs `DashboardViewModel` (Dagger / Koin as applicable — match existing module style for this view model)

## 2. Migration [SPORTS]

- [ ] 2.1 [BE] Add `orientationProvider: OrientationProvider` constructor param to `DashboardViewModel`
- [ ] 2.2 [BE] Replace the top-level `isPortrait()` call in `DashboardViewModel.buildNavigationBarItems` with `orientationProvider.isPortrait()`
- [ ] 2.3 [BE] Remove the `import org.dhis2.utils.isPortrait` line; leave the top-level function in place for other callers (Boy Scout rule applies to the touched file only)

## 3. Tests [SPORTS]

- [ ] 3.1 [BE] Update `DashboardViewModelTest` setup to construct the view model with a fake `OrientationProvider` that returns `true`
- [ ] 3.2 [BE] Add the deferred dispatcher test: `displayAnalytics resolves off the UI dispatcher within budget` — two `StandardTestDispatcher`s (ui, io), assert `pageConfigurator.displayAnalytics()` is NOT invoked after `uiDispatcher.advanceUntilIdle()`, IS invoked after `ioDispatcher.advanceUntilIdle()`, and the elapsed wall-clock time is under 200 ms
- [ ] 3.3 [BE] Run `./gradlew :app:testSportsDebugUnitTest` and confirm all `DashboardViewModelTest` tests pass

## 4. Validation [SPORTS]

- [ ] 4.1 [QA] Build `./gradlew :app:assembleSportsDebug`, install on reproducer device, open the Strength & Conditioning TEI — nav bar state unchanged (Details + Notes immediately, Analytics/Relationships after Phase 2)
- [ ] 4.2 [QA] Rotate device to landscape and re-open a TEI — `Details` tab hidden (as before), other tabs unchanged

## 5. Upstream contribution

- [ ] 5.1 [BE] Port to `develop-eyeseetea` alongside Fix A + Fix B if not yet contributed; otherwise as its own PR against Oslo's `develop`
