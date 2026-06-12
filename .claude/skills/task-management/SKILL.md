---
name: task-management
description: >
  Project management skill for creating and managing tasks in the issue tracker
  from OpenSpec artifacts. Use whenever creating tasks, updating status, planning
  sprints, or coordinating work between development agents. Trigger on any
  mention of tickets, tasks, sprint planning, or project tracking.
---

# Issue Tracker Task Management

## Creating Tasks from OpenSpec

When given an OpenSpec change proposal:

1. Read `openspec/changes/<change-name>/tasks.md` for the task breakdown
2. Read `openspec/changes/<change-name>/design.md` for technical context
3. For each task, create a task in the issue tracker with:
   - **Name**: `[ROLE] Task description`
   - **Description**: Include acceptance criteria from the spec
   - **Priority**: Based on dependency order (blocking tasks = high)
   - **Assignee**: Map to the appropriate agent role
   - **Tags**: Feature name, sprint number, target flavor/fork

## Role-to-Assignee Mapping

| Role Tag | Agent | Task Type |
|----------|-------|-----------|
| [UI]     | frontend-developer | Compose screens, UI components, Activities/Fragments |
| [BE]     | backend-developer | Use cases, repositories, SDK integration, business logic |
| [DB]     | database-manager | Data models, SDK queries, sync strategies |
| [GD]     | graphical-designer | Visual design, assets, theming |
| [UX]     | ux-designer | Wireframes, user flows, interaction specs |
| [CR]     | code-reviewer | Code review after implementation |
| [BUILD]  | android-build-specialist | Gradle, flavors, dependencies, CI |

## Task Dependencies
Create tasks in dependency order:
1. DB data model → BE domain logic → UI implementation
2. UX wireframes → GD mockups → UI implementation
3. BUILD flavor setup (if needed) → all other tasks
4. Both tracks can run in parallel

## Fork-Specific Tasks

When a task targets a specific fork, prefix accordingly:
- `[UI][WIDP] Custom enrollment screen`
- `[BE][PSI] Biometric authentication flow`

## Task Structure Strategy
Choose the structure based on feature complexity:

**Simple features** (roughly 5 or fewer tasks):
- Create ONE parent issue named: `[Feature name] - Brief description`
- Create subtasks under it for each individual task from tasks.md
- Subtask names: `[ROLE] Task description`

**Complex features** (more than 5 tasks, or multiple parallel tracks):
- Create multiple standalone issues
- Every issue title MUST include the feature name as a prefix so they can be filtered together
- Format: `[Feature name] [ROLE] Task description`

When in doubt, prefer the simple approach (parent + subtasks).
