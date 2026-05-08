---
name: refactoring
description: Refactor candidates to address after each TDD cycle passes.
type: reference
---

# Refactor Candidates (after TDD cycle)

- **Duplication** → Extract function/class
- **Long methods** → Break into private helpers (keep tests on public interface)
- **Shallow modules** → Combine or deepen
- **Feature envy** → Move logic to where data lives
- **Primitive obsession** → Introduce value objects
- **Existing code** the new code reveals as problematic
