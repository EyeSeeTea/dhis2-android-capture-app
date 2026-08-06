# URL data element field

## Purpose

DHIS2 data elements can carry a `url` attribute pointing to reference material (clinical guidelines, SOPs, decision trees). When a WIDP form displays such a field, the user needs to reach that URL directly from the info / description affordance of the field, without leaving the form context.

> **Current status: active** (rendering reimplemented on 2026-04-17 as part of `openspec/changes/upgrade-widp-to-3-3-1/`). The URL is appended to the field's supporting text in `FieldUiModelExtensions.supportingText()`, so it renders inline below the description in every Compose input field.

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

### Requirement: URL is displayed in the field supporting text
When a field has a non-null `url`, the app SHALL append it to the field's Compose supporting text so the user can read it inline with the description, without opening a separate dialog.

#### Scenario: Field with description and URL
- **WHEN** a field has both a non-blank `description` and a non-blank `url`
- **THEN** its `supportingText()` emits a single default-state entry containing `description + "\n" + url`

#### Scenario: Field with URL but no description
- **WHEN** a field has a blank `description` and a non-blank `url`
- **THEN** its `supportingText()` emits a single default-state entry containing the `url` on its own

#### Scenario: Field with description but no URL
- **WHEN** a field has a non-blank `description` and a blank or null `url`
- **THEN** its `supportingText()` emits the description alone, unchanged from stock behavior

#### Scenario: Field with neither description nor URL
- **WHEN** a field has neither a `description` nor a `url`
- **THEN** the default-state supporting text entry is omitted (no trailing blank line is rendered)
