#!/bin/bash
# Human-in-the-loop Ralph: run once, review the commit, then run again.

claude --permission-mode default "@ai/prds/rtometer.md @ai/progress.txt
1. Read the PRD and progress file.
2. Find the next incomplete task (lowest RTO number with no blockers remaining).
3. Follow TDD: write a failing test first, then implement until it passes, then refactor.
4. Run './gradlew testDebugUnitTest' — fix any failures before committing.
5. Commit your changes with the issue number in the subject (e.g. 'RTO-3: ...').
6. Mark the task as done in ai/progress.txt with a note.
ONLY DO ONE TASK AT A TIME."
