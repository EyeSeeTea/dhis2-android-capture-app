## ADDED Requirements

### Requirement: Print receipt from the event screen menu
The system SHALL offer a "Print receipt" option in the top-right menu of the event screen of the Distribution stage in the SP IP app. The option SHALL always be available, whether or not the event is completed, and printing SHALL NOT complete the event.

#### Scenario: Menu option is available
- **WHEN** the user opens the menu of a Distribution event, completed or not
- **THEN** a "Print receipt" option is shown

#### Scenario: Printing does not complete the event
- **WHEN** the user prints the receipt of an event that is not completed
- **THEN** the receipt is printed and the event remains not completed

### Requirement: Receipt content
The system SHALL generate the receipt for the event being viewed. The receipt SHALL contain the SP logo, the distribution name, the distribution point, the date and time, the ration card ID, the beneficiary name, the beneficiary signature, and the items given with their quantities. Only items that have a value SHALL be printed. The distribution name SHALL be the option name, not the stored option code. The ration card ID and the beneficiary name SHALL come from the tracked entity attributes; the rest from the event.

#### Scenario: Receipt built from the viewed event
- **WHEN** the user prints the receipt of a Distribution event
- **THEN** the printed receipt shows the distribution name, ration card ID, beneficiary name and one line per item that has a value, with its quantity

#### Scenario: Items without a value are omitted
- **WHEN** the event has no value for some items
- **THEN** those items do not appear on the receipt

#### Scenario: Other items
- **WHEN** the event has a description and a quantity for an "Other item"
- **THEN** the receipt shows that item using its description and quantity

#### Scenario: Signature is printed as an image
- **WHEN** the event has a recipient signature image
- **THEN** the receipt includes the signature image

#### Scenario: Missing signature (pending confirmation)
- **WHEN** the event has no signature
- **THEN** the receipt is still printed without it

### Requirement: Receipt printed as an image
The system SHALL render the receipt as an image sized for the printer roll so that the logo and the signature are included. Text SHALL be Latin characters.

#### Scenario: Receipt fits the roll
- **WHEN** a receipt is rendered
- **THEN** its width matches the printable width of the 57 mm printer roll

### Requirement: Receipt designed for 57 mm paper
The system SHALL lay out the receipt for 57 mm wide thermal paper, both in the plain-text first version and in the bitmap version.

#### Scenario: Plain-text lines fit the roll
- **WHEN** a receipt is formatted as plain text
- **THEN** no line is longer than the characters per line allowed by the 57 mm roll

### Requirement: One receipt per press, unlimited reprints
The system SHALL print one receipt per press and SHALL allow printing the same event again any number of times once the previous print has finished.

#### Scenario: Second press while printing
- **WHEN** the user presses "Print receipt" while a print is in progress
- **THEN** only one receipt is printed

#### Scenario: Reprint after finishing
- **WHEN** a print has finished and the user presses "Print receipt" again
- **THEN** another receipt is printed

### Requirement: Print on the printer paired to the tablet
The system SHALL print on the Bluetooth printer paired to the tablet (one printer per tablet), without asking the user to select a printer, and SHALL work offline.

#### Scenario: Printer available
- **WHEN** the user prints a receipt and the printer is available
- **THEN** one receipt is sent to that printer and a success message is shown

#### Scenario: No printer available
- **WHEN** the user prints a receipt and the printer cannot be reached
- **THEN** an error message states that no printer is connected and nothing is printed

#### Scenario: Printing fails
- **WHEN** the printer is reachable but printing fails
- **THEN** an error message is shown

### Requirement: Mark the event as printed
After a successful print, the system SHALL set the "Receipt Printed" flag of the event. Printing SHALL NOT depend on this flag: a failure to set it, or an event that cannot be edited, SHALL NOT change the outcome shown to the user.

#### Scenario: Flag set after printing
- **WHEN** a receipt is printed successfully
- **THEN** the event's "Receipt Printed" flag is set

#### Scenario: Flag cannot be set
- **WHEN** a receipt is printed successfully and the flag cannot be written
- **THEN** the success message is still shown and no error is displayed

#### Scenario: Print fails
- **WHEN** printing fails
- **THEN** the flag is not set

#### Scenario: Setting the flag does not complete the event
- **WHEN** the flag is set
- **THEN** the event keeps its previous completion status

### Requirement: No pairing flow in the app
The system SHALL NOT provide its own printer pairing or connection screen; pairing is done in the Android Bluetooth settings.

#### Scenario: Pairing handled by the OS
- **WHEN** the user needs to pair a printer
- **THEN** the app relies on the printer already paired in the Android settings
