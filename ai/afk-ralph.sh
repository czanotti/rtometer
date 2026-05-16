#!/bin/bash
# AFK Ralph: runs N iterations unattended.
# Usage: ./afk-ralph.sh 10
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <iterations>"
  exit 1
fi

for ((i=1; i<=$1; i++)); do
  echo "--- Ralph iteration $i of $1 ---"

  result=$(docker sandbox run claude --permission-mode acceptEdits -p "@ai/prds/rtometer.md @ai/progress.txt
1. Read the PRD and progress file.
2. Find the next incomplete task (lowest RTO number with all blockers resolved).
3. Follow TDD: write a failing test first, then implement until it passes, then refactor.
4. Run './gradlew testDebugUnitTest' — fix any failures before committing.
5. Commit your changes with the issue number in the subject (e.g. 'RTO-3: ...').
6. Mark the task as done in ai/progress.txt with a one-line note.
ONLY WORK ON A SINGLE TASK.
If all tasks in ai/progress.txt are complete, output <promise>COMPLETE</promise>.")

  echo "$result"

  if [[ "$result" == *"<promise>COMPLETE</promise>"* ]]; then
    echo "All tasks complete after $i iterations."
    exit 0
  fi
done

echo "Reached $1 iterations. Check ai/progress.txt for current state."
