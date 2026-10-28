# spIP Validation Checklist

Manual validation checklist for the spIP fork (flavor `spIP`, application id `org.samaritan.platform`).

Only customizations documented in `eyeseetea-docs/customizations/spip/customization-files.md` have a flow here. Flows for the pre-existing customizations are pending and will be written during the next upgrade (see the pending item in that file).

No flows documented yet. Add one section per customization, using the exact title of its OpenSpec spec:

### N. [Customization title]

Preconditions:
- Starting state, user, data, or navigation point.

Manual flow:
1. Step one.
2. Step two.

Expected result:
- Expected visible or functional outcome.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
