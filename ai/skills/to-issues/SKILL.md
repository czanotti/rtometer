---
name: to-issues
description: Break a plan, spec, or PRD into independently-grabbable GitHub issues using tracer-bullet vertical slices. Use when user wants to convert a plan into issues, create implementation tickets, or break down work into issues.
---

# To Issues

Break a plan into independently-grabbable GitHub issues using vertical slices (tracer bullets).

## Process

### 1. Gather context

Work from whatever is already in the conversation context. If the user passes a GitHub issue number or URL as an argument, fetch it with `gh issue view <number>` (with comments).

### 2. Explore the codebase (optional)

If you have not already explored the codebase, do so to understand the current state of the code.

### 3. Draft vertical slices

Break the plan into **tracer bullet** issues. Each issue is a thin vertical slice that cuts through ALL integration layers end-to-end, NOT a horizontal slice of one layer.

Slices may be 'HITL' or 'AFK'. HITL slices require human interaction, such as an architectural decision or a design review. AFK slices can be implemented and merged without human interaction. Prefer AFK over HITL where possible.

<vertical-slice-rules>
- Each slice delivers a narrow but COMPLETE path through every layer (schema, API, UI, tests)
- A completed slice is demoable or verifiable on its own
- Prefer many thin slices over few thick ones
</vertical-slice-rules>

### 4. Quiz the user

Present the proposed breakdown as a numbered list. For each slice, show:

- **Title**: short descriptive name
- **Type**: HITL / AFK
- **Blocked by**: which other slices (if any) must complete first
- **User stories covered**: which user stories this addresses (if the source material has them)

Ask the user:

- Does the granularity feel right? (too coarse / too fine)
- Are the dependency relationships correct?
- Should any slices be merged or split further?
- Are the correct slices marked as HITL and AFK?

Iterate until the user approves the breakdown.

### 5. Create the GitHub issues

#### Determine the project prefix and next counter

Before creating any issues:

1. Derive the **project prefix**: uppercase the GitHub repo name (e.g. `terra` → `TERRA`). If the repo name is ambiguous or multi-word, use the shortest meaningful uppercase token.
2. Determine the **next counter**: run `gh issue list --state all --limit 1 --json number` and take `number + 1` as the starting counter. If no issues exist, start at `1`.

Issue identifiers follow the pattern `PREFIX-N` (e.g. `TERRA-1`, `TERRA-2`). The counter increments by one for each issue created in this batch, in dependency order.

#### Create each issue

For each approved slice, create a GitHub issue using `gh issue create`. Use the issue body template below.

Create issues in dependency order (blockers first) so you can reference real issue numbers in the "Blocked by" field.

The **title** must start with the issue identifier: `PREFIX-N: <short descriptive title>`.

<issue-template>
## Parent

#<parent-issue-number> (if the source was a GitHub issue, otherwise omit this section)

## What to build

A concise description of this vertical slice. Describe the end-to-end behavior, not layer-by-layer implementation.

## Acceptance criteria

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## Blocked by

- Blocked by PREFIX-N (#<issue-number>) (if any)

Or "None - can start immediately" if no blockers.

</issue-template>

Do NOT close or modify any parent issue.

#### Commit message convention

Commits that implement or relate to an issue must begin with the issue identifier:

```
TERRA-N: <imperative short description>
```