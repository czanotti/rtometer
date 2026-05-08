---
name: mocking
description: Rules for when and how to mock — system boundaries only, never internal collaborators.
type: reference
---

# When to Mock

Mock at **system boundaries only:**
- External APIs (payment, email, etc.)
- Databases (sometimes — prefer test DB)
- Time/randomness
- File system (sometimes)

**Do NOT mock:**
- Your own classes/modules
- Internal collaborators
- Anything you control

## Designing for Mockability

1. **Use dependency injection** — pass external dependencies in rather than creating them internally
2. **Prefer SDK-style interfaces over generic fetchers** — create specific functions for each external operation instead of one generic function with conditional logic:

```typescript
// GOOD: Each function is independently mockable
const api = {
  getUser: (id) => fetch(`/users/${id}`),
  getOrders: (userId) => fetch(`/users/${userId}/orders`),
  createOrder: (data) => fetch('/orders', { method: 'POST', body: data }),
};

// BAD: Mocking requires conditional logic inside the mock
const api = {
  fetch: (endpoint, options) => fetch(endpoint, options),
};
```
