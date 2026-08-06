# Image upload without resizing

## Purpose

Clinical and field photos captured in the WIDP programme must preserve original quality and resolution for diagnostic and documentation purposes. The stock DHIS2 Android client compresses and resizes images before upload; this behavior is explicitly disabled for the `widp` flavor.

## Requirements

### Requirement: No client-side resizing of captured images
The app SHALL upload images at the resolution they were captured, without applying any client-side downscaling, resampling, or dimension reduction.

#### Scenario: Image captured from camera is uploaded full-size
- **WHEN** the user captures a photo through the in-app camera flow inside a data entry form
- **THEN** the image stored locally and uploaded to the server has the same pixel dimensions as the original capture

#### Scenario: Image picked from gallery is uploaded full-size
- **WHEN** the user attaches an image from the device gallery inside a data entry form
- **THEN** the image stored locally and uploaded to the server has the same pixel dimensions as the source file

### Requirement: No client-side compression of captured images
The app SHALL NOT re-encode images through a lossy pipeline (quality reduction, JPEG re-compression) before saving them into the form value store.

#### Scenario: Uploaded image file size matches source
- **WHEN** an image is saved into a form value
- **THEN** its serialized bytes are not the result of a lossy re-encode applied by the app (bit-identical content is not required, but the app SHALL NOT deliberately downgrade quality)

### Requirement: Applies uniformly to all image-type form fields
The no-resize behavior SHALL apply to every image-type data element and every image attachment mechanism in the form, regardless of whether the source is the camera, the gallery, or a file picker.

#### Scenario: Multiple image fields in a single form
- **WHEN** a form contains several image-type data elements and the user fills all of them
- **THEN** every image is uploaded with its original resolution and without compression
