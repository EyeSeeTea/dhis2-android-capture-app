---
name: clickup-pm
description: >
  Project management skill for creating and managing ClickUp tasks from
  OpenSpec artifacts. Use whenever creating tasks, updating status, planning
  sprints, or coordinating work between development agents. Trigger on any
  mention of tickets, tasks, sprint planning, or project tracking.
---

# ClickUp Project Management

## Creating Tasks from OpenSpec

When given an OpenSpec change proposal:

1. Read `openspec/changes/<change-name>/tasks.md` for the task breakdown
2. Read `openspec/changes/<change-name>/design.md` for technical context
3. For each task, create a ClickUp task with:
   - **Name**: `[ROLE] Task description`
   - **Description**: Include acceptance criteria from the spec
   - **Priority**: Based on dependency order (blocking tasks = high)
   - **Assignee**: Map to the appropriate agent role
   - **Tags**: Feature name, sprint number, target flavor/fork

## Role-to-Assignee Mapping

| Role Tag | Agent | Task Type |
|----------|-------|-----------|
| [UI]     | frontend-developer | Compose screens, UI components, Activities/Fragments |
| [DOMAIN] | backend-developer | Use cases, repositories, SDK integration, business logic |
| [DATA]   | database-manager | Data models, SDK queries, sync strategies |
| [UX]     | ux-designer | Wireframes, user flows, interaction specs |
| [GD]     | graphical-designer | Visual design, assets, theming |
| [BUILD]  | android-build-specialist | Gradle, flavors, dependencies, CI |

## Task Dependencies
Create tasks in dependency order:
1. DATA model → DOMAIN use cases → UI implementation
2. UX wireframes → GD mockups → UI implementation
3. BUILD flavor setup (if needed) → all other tasks
4. Both tracks can run in parallel

## Fork-Specific Tasks
When a task is fork-specific, prefix with the fork name:
- `[UI][WIDP] Custom enrollment screen`
- `[DOMAIN][PSI] Biometric authentication flow`
