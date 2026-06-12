# Sports Branding

## Context

The sports fork requires distinct visual branding to differentiate it from the default DHIS2 app and other EyeSeeTea forks. This includes a custom launcher icon, a sports-specific app name localized in approximately 30 locales, and a distinct debug icon so testers can visually distinguish debug builds from release builds on the same device.

Branding assets live entirely within the `app/src/sports/` and `app/src/sportsDebug/` flavor source sets, meaning they override the default resources at build time without modifying shared code.

## Requirements

### REQ-SB-01: Sports launcher icon

The `sports` product flavor MUST provide a custom adaptive launcher icon (`ic_launcher`) that displays the sports-specific foreground graphic (`ic_sports_foreground`) over the sports-specific background color (`ic_launcher_background`).

The icon MUST be provided in all standard Android density buckets: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, and as an adaptive icon definition in `mipmap-anydpi-v26/`.

A round variant (`ic_launcher_round`) MUST also be provided at all density buckets.

### REQ-SB-02: Localized app name

The `sports` flavor MUST define the `app_name` string resource as "Sports tracker" in the default locale (`values/strings.xml`).

The `sports` flavor MUST provide localized `app_name` translations in all supported locales. At minimum, the following locale qualifiers SHALL have a `strings.xml` with an `app_name` entry: `ar`, `ar-rIQ`, `b+es+419`, `bn`, `ckb`, `cs`, `da`, `es`, `fr`, `hi-rIN`, `id`, `km`, `lo`, `my`, `nb`, `ne`, `nl`, `or`, `prs`, `ps`, `pt`, `pt-rBR`, `ro`, `ru`, `si`, `sv`, `tet`, `tg`, `uk`, `ur`, `uz`, `uz-rUZ`, `vi`, `zh`, `zh-rCN`.

### REQ-SB-03: Debug build icon differentiation

The `sportsDebug` build type overlay MUST provide a distinct launcher icon configuration so that debug builds are visually distinguishable from release builds when both are installed on the same device.

The debug icon MUST use the same sports foreground graphic but MAY use a different background or overlay to signal debug status.

## Scenarios

### Scenario: Release build displays sports icon

- **GIVEN** the app is built with the `sports` product flavor in `release` build type
- **WHEN** the user views the device home screen or app drawer
- **THEN** the app icon displays the sports-specific foreground graphic on the sports background color

### Scenario: Debug build displays distinguishable icon

- **GIVEN** the app is built with the `sports` product flavor in `debug` build type
- **WHEN** the user views the device home screen or app drawer
- **THEN** the app icon is visually distinguishable from the release sports icon

### Scenario: App name displayed in user locale

- **GIVEN** the device locale is set to one of the supported locales (e.g., `fr`)
- **WHEN** the user views the app name in the launcher, app settings, or recent apps
- **THEN** the app name is displayed in the localized translation for that locale

### Scenario: App name fallback for unsupported locale

- **GIVEN** the device locale is set to a locale not in the supported list
- **WHEN** the user views the app name
- **THEN** the app name falls back to "Sports tracker" (the default English value)
