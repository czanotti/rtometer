---
name: deep-modules
description: Reference for designing deep modules with simple interfaces and rich internal logic.
type: reference
---

# Deep Modules (from "A Philosophy of Software Design")

Create interfaces that are **simple and minimal while containing substantial implementation complexity**.

- **Deep Module (good):** Small interface + lots of implementation. Few methods with straightforward parameters. Hidden complexity within the module.
- **Shallow Module (avoid):** Large interface with minimal implementation. Many methods and complex parameters. Delegates work rather than encapsulating it.

Design questions to ask:
- Can the number of methods be reduced?
- Can parameters be simplified?
- Can more complexity be concealed within the module?
