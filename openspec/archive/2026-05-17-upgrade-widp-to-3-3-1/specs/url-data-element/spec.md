## MODIFIED Requirements

### Requirement: Reimplementation in Compose UI
Because the legacy `onDescriptionClick` pathway and `RecyclerViewUiEvents.ShowDescriptionLabelDialog` were removed upstream, the current Compose-based form UI SHALL reimplement the URL display by hooking into whichever Compose component renders the field description in the post-3.3.1 codebase. The implementation SHALL follow the customization placement hierarchy: prefer a self-contained Compose modifier or wrapper over inline edits in Oslo Compose components.

#### Scenario: URL plumbing without rendering
- **WHEN** a field has a non-null `url` but the Compose renderer has no code path for it
- **THEN** this capability is considered broken — the spec is NOT satisfied, and the failure SHALL be tracked as an open issue until the renderer is updated

#### Scenario: URL rendered in Compose info dialog
- **WHEN** the user taps the info icon of a field whose `FieldUiModel.url` is non-null in the Compose-based form
- **THEN** the description surface shows `description + "\n" + url` and the URL is tappable to open in a browser
