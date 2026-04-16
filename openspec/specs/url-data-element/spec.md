# URL data element field

## Purpose

DHIS2 data elements can carry a `url` attribute pointing to reference material (clinical guidelines, SOPs, decision trees). When a WIDP form displays such a field, the user needs to reach that URL directly from the info / description affordance of the field, without leaving the form context.

> **Current status: broken.** The data plumbing is intact (the URL is read from the SDK and stored in the field UI model), but the rendering was lost during the upstream migration of the forms to Jetpack Compose. This spec captures the intended behavior; reimplementing the rendering is tracked in `openspec/changes/upgrade-widp-to-3.3.1/`.

## Requirements

### Requirement: URL is read from the SDK for event data elements
For event forms, the app SHALL read `url` from the SDK `DataElement` and propagate it through the field factory into `FieldUiModel.url`.

#### Scenario: Event data element with URL
- **WHEN** an event form is built and one of its data elements has a non-null `url`
- **THEN** the corresponding `FieldUiModel` has `url` populated with the same value

#### Scenario: Event data element without URL
- **WHEN** an event data element has a null `url`
- **THEN** the corresponding `FieldUiModel.url` is null

### Requirement: Enrollment forms do not carry URLs
For enrollment forms, the app SHALL pass `url = null` for every field. URLs only apply to event data elements in this capability.

#### Scenario: Enrollment form built
- **WHEN** an enrollment form is built
- **THEN** every field's `url` is null, even when the underlying attribute has a URL defined

### Requirement: URL is displayed in the field description affordance
When the user taps the info / description icon of a field whose `url` is non-null, the app SHALL show a dialog or surface that contains the description text followed by the URL on a new line. The URL SHALL be selectable or actionable (tap to open in a browser).

#### Scenario: Tap info on a field with URL
- **WHEN** the user taps the info icon of a field whose `FieldUiModel.url` is non-null
- **THEN** the resulting dialog shows `description + "\n" + url` and the URL is tappable

#### Scenario: Tap info on a field without URL
- **WHEN** the user taps the info icon of a field whose `FieldUiModel.url` is null
- **THEN** the dialog shows only the description — no trailing URL line is rendered

### Requirement: Reimplementation in Compose UI
Because the legacy `onDescriptionClick` pathway and `RecyclerViewUiEvents.ShowDescriptionLabelDialog` were removed upstream, the current Compose-based form UI SHALL reimplement the URL display by hooking into whichever Compose component renders the field description in the post-migration codebase.

#### Scenario: URL plumbing without rendering
- **WHEN** a field has a non-null `url` but the Compose renderer has no code path for it
- **THEN** this capability is considered broken — the spec is NOT satisfied, and the failure SHALL be tracked as an open issue until the renderer is updated
