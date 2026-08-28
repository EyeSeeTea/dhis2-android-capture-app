# OCA Validation Checklist

Manual validation checklist for the known OCA customizations after an upgrade.

Use this file for:
- minimal manual test flows
- expected results
- identifying regressions after merge resolution

Do not use this file for:
- merge progress
- raw diff inventory
- implementation notes

## Status

No confirmed OCA-specific functional customizations exist yet (see `eyeseetea-docs/customizations/oca/customization-files.md`). Add one `## N. [Customization title]` section here, matching the title in `openspec/specs/<capability>/spec.md`, whenever a real OCA customization is confirmed.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `eyeseetea-docs/customizations/oca/customization-files.md`
