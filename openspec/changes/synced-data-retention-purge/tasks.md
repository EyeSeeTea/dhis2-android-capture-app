## 1. Capability flag

- [ ] 1.1 Add a behavior-level test asserting the retention purge capability
  is disabled when the shared build-time flag is off, and verify it fails
  (red) before any flag exists.
- [ ] 1.2 Add the shared, build-time capability flag (off by default, no
  flavor-specific file) and make the test from 1.1 pass.
  **Commit:** 1.1 + 1.2 are one commit (red -> green).

## 2. Background job: schedule and manual trigger

- [ ] 2.1 Add a test asserting that scheduling the retention purge job with
  a given frequency enqueues a periodic background job with that interval,
  and verify it fails before the scheduling function exists.
- [ ] 2.2 Implement the scheduling function (periodic job, configurable
  frequency) reusing the existing background-job infrastructure, and make
  the test from 2.1 pass.
  **Commit:** 2.1 + 2.2 are one commit (red -> green).
- [ ] 2.3 Add a test asserting that a manual "run now" trigger enqueues an
  immediate one-off run of the same job, and verify it fails before the
  trigger function exists.
- [ ] 2.4 Implement the manual trigger function, and make the test from 2.3
  pass.
  **Commit:** 2.3 + 2.4 are one commit (red -> green).
- [ ] 2.5 Add a test asserting that a manual trigger while a purge is
  already running does not enqueue a second concurrent run, and verify it
  fails before the guard exists.
- [ ] 2.6 Implement the double-run guard (unique-work policy), and make the
  test from 2.5 pass.
  **Commit:** 2.5 + 2.6 are one commit (red -> green).

## 3. Background job: invoke the SDK purge

- [ ] 3.1 Add a test asserting that the background job invokes
  `D2.retentionModule().purge()` and reports success/failure accordingly,
  and verify it fails before the job's work logic exists.
- [ ] 3.2 Implement the job's work logic calling
  `D2.retentionModule().purge()`, and make the test from 3.1 pass.
  **Commit:** 3.1 + 3.2 are one commit (red -> green).
- [ ] 3.3 Add a test asserting that a successful run persists a "last purge"
  timestamp, and verify it fails before that persistence exists.
- [ ] 3.4 Implement persistence of the last-purge timestamp, and make the
  test from 3.3 pass.
  **Commit:** 3.3 + 3.4 are one commit (red -> green).

## 4. Settings UI: schedule, manual trigger, status

- [ ] 4.1 Add the purge schedule/status row to the Sync Manager settings
  screen (frequency control, "run now" action, last-run timestamp,
  in-progress indicator), wired to the functions from groups 2-3, and
  verify it renders and reflects state changes via a UI/state test.
  **Commit:** its own commit (UI wiring, no pre-existing behavior to test
  first against).
- [ ] 4.2 Add a test asserting the purge row is hidden/absent when the
  capability flag from group 1 is disabled, and verify it fails before the
  conditional rendering exists.
- [ ] 4.3 Implement the conditional rendering gated by the capability flag,
  and make the test from 4.2 pass.
  **Commit:** 4.2 + 4.3 are one commit (red -> green).

## 5. Documentation

- [ ] 5.1 Update `eyeseetea-docs/` customization-files inventories for each
  fork that enables the capability, to list the new shared files this
  change adds/touches (background job wiring, settings screen row,
  capability flag) so the fork's technical file-level inventory stays
  accurate.
- [ ] 5.2 Add a manual-validation entry (schedule change, manual trigger,
  status visibility, capability disabled by default) to each enabling
  fork's `upgrade-validation-checklist.md`.
  **Commit:** 5.1 + 5.2 together as one documentation commit, after
  implementation is verified.
