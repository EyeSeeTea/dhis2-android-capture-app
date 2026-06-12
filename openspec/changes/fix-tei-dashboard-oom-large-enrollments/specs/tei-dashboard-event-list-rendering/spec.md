# tei-dashboard-event-list-rendering

## ADDED Requirements

### Requirement: Initial event list render is capped

The TEI dashboard data tab SHALL render at most a fixed initial number of event cards per program stage in grouped mode (3) and overall in timeline mode (5), regardless of how many events the enrollment contains.

#### Scenario: Opening a large enrollment

- **WHEN** the user opens the TEI dashboard of an enrollment with 1000+ events
- **THEN** only the capped subset of event cards is bound to the list, the stage header shows the total event count, and the screen renders without ANR

### Requirement: Show more reveals events incrementally

The "show more" affordance SHALL reveal events in pages of 25 instead of expanding to the full event list in a single action. The remaining-count SHALL be visible on the affordance. A "show less" affordance SHALL collapse the stage (or timeline) back to the initial cap.

#### Scenario: Paging through a large stage

- **WHEN** the user taps "show more" on a stage with 1036 events
- **THEN** 25 additional event cards are revealed (28 visible), the affordance shows the remaining count, and the app remains responsive

#### Scenario: Collapsing a revealed stage

- **WHEN** the user taps "show less" after revealing several pages
- **THEN** the stage returns to its initial capped view

### Requirement: Revealed window is bounded, never the full list by default

No single user action SHALL cause the adapter to bind an unbounded number of event cards. The number of bound cards after any action SHALL equal the previously revealed count plus at most one page.

#### Scenario: No binary expand-all path remains

- **WHEN** any "show more" control in grouped or timeline mode is activated
- **THEN** the visible window grows by exactly one page (25), and `showAllEvents`-style full expansion is not reachable from the UI

### Requirement: Small enrollments are visually unchanged

Enrollments whose stages fit within the initial cap SHALL render identically to the pre-change behavior, with no paging affordances shown.

#### Scenario: Small enrollment

- **WHEN** the user opens an enrollment where every stage has 3 or fewer events
- **THEN** no "show more"/"show less" affordances appear and the list matches pre-change rendering
