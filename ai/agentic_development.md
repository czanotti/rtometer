1. Management of LLM Constraints
The Smart Zone vs. Dumb Zone: LLMs perform best at the start of a session. As context grows (approaching ~100k tokens), intelligence degrades quadratically. Instruction: Keep sessions short and specific; clear context frequently to reset to the "smart zone" [04:18].

Avoid Compacting: Don't just summarize old chats into the current context; it creates "sediment." Instruction: Prefer starting fresh with a written record (an asset) rather than a condensed chat history [10:43].

2. Planning & Alignment (Human-in-the-Loop)
The "Grill Me" Skill: Before coding, Claude must relentlessly interview you to reach a "shared design concept." Instruction: Claude should ask one question at a time about edge cases, retroactive data, and UI placement until a deep understanding is reached [16:22].

The Destination Document (PRD): Summarize the grilling session into a Product Requirements Document (PRD). Focus on problem statements, user stories, and specific implementation/testing decisions [30:28].

The Journey Document (Kanban/Issues): Break the PRD into independent, "grabbable" issues. Instruction: Use Vertical Slices (Tracer Bullets)—each task must touch every layer (DB, API, UI) to provide immediate feedback, rather than coding horizontally layer-by-layer [43:48].

3. Implementation (AFK/Agentic Development)
AFK Mode: Once the backlog is set, the agent should work "Away From Keyboard." Instruction: Run Claude in a loop (e.g., via a bash script or sandbox) to pick up the next task from the backlog, prioritizing critical bugs then vertical slices [56:15].

TDD (Test-Driven Development): This is the "ceiling" of AI quality. Instruction: Claude must follow a Red-Green-Refactor loop. Write a failing test first, then the implementation, then verify [01:07:00].

Automated Review: Clear context after implementation and use a fresh, "smarter" model (e.g., Claude Opus) to review the code against standards before the human QA [01:30:40].

4. Architectural Standards
Deep Modules: Avoid "shallow" modules (many small files with complex dependencies). Instruction: Build "Deep Modules" with simple interfaces and rich internal logic. This makes the codebase easier for agents to navigate and test [01:17:07].

Push vs. Pull:

Pull: Allow the implementing agent to "pull" info from a repository of "skills" or documentation when needed.

Push: "Push" coding standards directly into the prompt of the Reviewer agent to ensure compliance [01:29:03].

5. Tracer Bullets & Traces

Tracer Bullets — How You Build
From The Pragmatic Programmer. A tracer bullet is a tiny, vertical slice of functionality that goes end-to-end (UI → API → DB).

The Problem: AI agents love "slop." They try to write 200 lines at once, outrunning their headlights and making errors that are hard to debug.
The Fix: Force the agent to build one small thing that actually works first. Once that glow-in-the-dark bullet hits the target, build the rest of the features around that proven path.

Traces — How You Observe
A trace is the recorded journey of a single request through your system (LangFuse, Arize, etc.). It captures:
- The Chain: exactly which prompts were sent and in what order
- The Latency: how long each step took
- The Cost: tokens consumed at each hop
- The Failure Point: the exact moment the agent hallucinated or diverged

The Loop (Aim → Fire → Inspect → Adjust):
| Step | Action | Tool/Concept |
|------|--------|--------------|
| 1. Aim | Define a tiny vertical slice | Tracer Bullet |
| 2. Fire | Let the agent implement that slice | Claude Code |
| 3. Inspect | Look at execution logs for failures | Traces (LangFuse/Arize) |
| 4. Adjust | Fix the prompt or logic and fire again | Evals |

Tracer bullets keep the agent from getting lost in the dark; traces are the flashlight that shows where the bullet landed.

Key Summary for your Claude System Prompt:
"Work in the 'Smart Zone' by keeping tasks small. Always start by 'grilling' me for alignment. Plan using Vertical Slices (Tracer Bullets) that provide end-to-end feedback. Implement using TDD (Red-Green-Refactor). Aim for 'Deep Modules' with simple interfaces to maintain architectural sanity. After each vertical slice, inspect traces to see exactly where the agent diverged."