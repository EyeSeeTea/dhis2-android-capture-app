## ADDED Requirements

### Requirement: Print receipt from the TEI dashboard menu
The system SHALL offer a "Print receipt" option in the TEI dashboard menu of the SP IP app.

#### Scenario: Menu option is available
- **WHEN** the user opens the TEI dashboard menu
- **THEN** a "Print receipt" option is shown

### Requirement: Receipt content
The system SHALL generate the receipt from the data values of a completed event, as ordered label and value lines. The event is expected to hold the distribution, the distribution point, the receiver and their relation to the beneficiary, the verification performed, the real household size and the quantity delivered of each commodity.

#### Scenario: Receipt built from a completed event
- **WHEN** the user prints a receipt for a completed event
- **THEN** the printed receipt contains a label and value line for each data value of that event

### Requirement: Receipt for beneficiary and program staff
The system SHALL produce a receipt that is issued to both the beneficiary and the program staff.

#### Scenario: Receipt covers both recipients
- **WHEN** the user prints a receipt for a completed event
- **THEN** the output serves both the beneficiary and the program staff (number of physical copies to be confirmed)

### Requirement: Print on the connected printer
The system SHALL print one receipt per action on the Bluetooth printer that is currently connected, without asking the user to select a printer, and SHALL work offline.

#### Scenario: Several printers paired, one connected
- **WHEN** several printers are paired with the device and only one is connected
- **THEN** the receipt is printed on the connected one

#### Scenario: Printer connected
- **WHEN** the user prints a receipt and a printer is connected
- **THEN** one receipt is sent to that printer and a success message is shown

#### Scenario: No printer connected
- **WHEN** the user prints a receipt and no printer is connected
- **THEN** an error message states that no printer is connected and nothing is printed

#### Scenario: Printing fails
- **WHEN** the printer is connected but printing fails
- **THEN** an error message is shown

### Requirement: No pairing flow in the app
The system SHALL NOT provide its own printer pairing or connection screen; pairing and connection are done in the Android Bluetooth settings.

#### Scenario: Pairing handled by the OS
- **WHEN** the user needs to connect a printer
- **THEN** the app relies on the printer already connected in the Android settings
