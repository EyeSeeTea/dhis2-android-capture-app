# Lot Number Field

## Purpose

Nurses recording medication administration in the UNICEF TJK eLMIS event form must record the lot number of each product administered, so that consumption can be reconciled against stock by lot when the event is uploaded. This capability augments the "Lot Number" text field with a search dialog that suggests lot numbers known to be available, for the event's organisation unit and the product selected elsewhere in the same event — sourced from data the OpenBoxes ↔ DHIS2 sync already publishes to the DHIS2 datastore. The suggestion is always assistive: the field remains a plain editable text field regardless of what the dialog finds.

This capability attaches to one specific DataElement in the UNICEF TJK eLMIS programme and reads the value of another specific DataElement (the selected product) in the same event. It cannot live entirely in the flavor source set because the form's field-rendering dispatcher and the metadata-sync entry point are both shared code with no existing per-field or per-flavor extension point; this capability's spec covers the resulting behavior regardless of where the implementation places its files.

## ADDED Requirements

### Requirement: The Lot Number field SHALL present a search affordance alongside the editable text field

The designated "Lot Number" DataElement SHALL render as an always-editable text field accompanied by a search button. The text field SHALL accept manually typed values at any time, independent of whether the search dialog has been opened or what it contains.

#### Scenario: Field is editable without using search

- **WHEN** the user taps into the Lot Number field and types a value directly
- **THEN** the typed value is accepted and behaves like any other text DataElement value (subject to the form's normal save/validation rules)

#### Scenario: Search button is always present

- **WHEN** the Lot Number field is displayed
- **THEN** a search button is shown next to the text field, regardless of whether a product has been selected elsewhere in the event

### Requirement: The search dialog SHALL resolve available lot numbers by the event's organisation unit and the selected product

When the user taps the search button, the app SHALL determine:
- the organisation unit code of the event being edited, and
- the product code currently recorded in the designated "Product" DataElement of the same event,

and SHALL use both to look up available lot numbers.

#### Scenario: Product already selected before opening the dialog

- **WHEN** the user has selected a product in the "Product" field and then taps the Lot Number search button
- **THEN** the dialog looks up lot numbers using the event's organisation unit and the selected product's code

#### Scenario: No product selected yet

- **WHEN** the user taps the Lot Number search button before selecting any value in the "Product" field
- **THEN** the dialog indicates that a product must be selected first
- **AND** the dialog does not attempt to look up or display a lot number list
- **AND** the underlying Lot Number text field remains editable

### Requirement: Lot number data SHALL be fetched network-first with a local cache fallback

The app SHALL attempt to retrieve the current `available-lot-numbers` dataset from the DHIS2 server when the search dialog is opened with a resolved product. If that attempt fails (no connectivity or a server error), the app SHALL use the most recently cached copy of the dataset instead. A successful server fetch SHALL update the local cache.

#### Scenario: Online lookup succeeds

- **WHEN** the device has connectivity and the server responds successfully to the lot-numbers lookup
- **THEN** the dialog displays lot numbers based on the fresh server response
- **AND** the local cache is updated with that response

#### Scenario: Online lookup fails, cache has data

- **WHEN** the device has no connectivity, or the server request fails
- **AND** a previously cached lot-numbers dataset exists
- **THEN** the dialog displays lot numbers based on the cached dataset

#### Scenario: Online lookup fails, no cache available

- **WHEN** the device has no connectivity, or the server request fails
- **AND** no cached lot-numbers dataset exists yet
- **THEN** the dialog behaves as if no lot numbers were found for the resolved organisation unit and product (see "no lot numbers found")

### Requirement: The local lot-number cache SHALL be refreshed proactively during metadata synchronization

In addition to being refreshed opportunistically after a successful online lookup, the local lot-number cache SHALL be refreshed as part of the app's regular metadata synchronization process, so that lot-number data is reasonably current even if the user never opens the search dialog.

#### Scenario: Metadata sync refreshes the cache

- **WHEN** a metadata synchronization completes successfully and the lot-numbers dataset is reachable
- **THEN** the local lot-number cache is updated with the retrieved dataset

#### Scenario: Metadata sync runs without affecting other sync steps

- **WHEN** the lot-numbers dataset is unreachable during a metadata synchronization
- **THEN** the metadata synchronization SHALL still complete successfully for its other steps
- **AND** the previously cached lot-number dataset (if any) SHALL remain unchanged

### Requirement: The search dialog SHALL distinguish "no lot numbers found" from "list available" and SHALL always allow manual entry

When a product code has been resolved and a lookup (online or cached) has completed, the dialog SHALL show one of two states:
- a selectable list of lot numbers, when at least one lot number is available for the resolved organisation unit and product; or
- an indication that no lot numbers were found for that organisation unit and product, together with guidance that the user can type the lot number manually.

In both states, and in the "no product selected" state, the underlying Lot Number text field SHALL remain directly editable, and dismissing the dialog SHALL NOT clear or alter a manually typed value.

#### Scenario: Lot numbers available

- **WHEN** the resolved organisation unit and product have one or more lot numbers in the dataset (online or cached)
- **THEN** the dialog displays them as a selectable list

#### Scenario: Selecting a lot number from the list

- **WHEN** the user selects a lot number from the dialog's list
- **THEN** the Lot Number text field is set to the selected value
- **AND** the dialog closes

#### Scenario: Cancelling the dialog

- **WHEN** the user dismisses or cancels the dialog without selecting a lot number
- **THEN** the dialog closes
- **AND** the Lot Number text field's current value (whether empty or manually typed) is unchanged

#### Scenario: No lot numbers found for the resolved organisation unit and product

- **WHEN** the resolved organisation unit and product have no entry, or an empty lot-number list, in the dataset (online or cached)
- **THEN** the dialog indicates that no lot numbers were found
- **AND** the dialog indicates that the user can enter the lot number manually
- **AND** the Lot Number text field remains editable for manual entry
