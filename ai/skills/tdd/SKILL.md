---
name: tdd
description: Guide Test-Driven Development using vertical slices and tracer bullets. Use when implementing any new feature or module.
---

Core philosophy: "Tests should verify behavior through public interfaces, not implementation details." Good tests are integration-style and read like specifications. Bad tests couple to implementation—breaking when refactored even though behavior is unchanged.

**Critical Anti-Pattern: Horizontal Slicing** — Writing all tests upfront then all implementation. This produces poor tests that verify imagined rather than actual behavior. Instead, use vertical slices with tracer bullets: one test, one implementation cycle, repeat.

**Structured Workflow:**
- **Planning** — confirm interfaces with stakeholders, prioritize which behaviors to test
- **Tracer Bullet** — prove end-to-end path with a single test-code pair
- **Incremental Loop** — continue one test at a time with minimal code additions; avoid speculative features
- **Refactor** — only after tests pass; focus on eliminating duplication and deepening module design

Each cycle should verify: tests describe behavior (not implementation), use only public interfaces, and would survive internal refactoring.
