#!/bin/bash

set -u

mkdir -p workspace/logs workspace/failures

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "===================================="

AI_CONTEXT=$(cat AI_CONTEXT.md)
DEV_SKILL=$(cat skills/dev.md)
TESTER_SKILL=$(cat skills/tester.md)
FIXER_SKILL=$(cat skills/fixer.md)

MAX_FIX_ATTEMPTS=2
SUMMARY_FILE="workspace/pipeline_summary.md"

echo "# AI Pipeline Summary" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

for FILE in workspace/features/*.md; do
  FEATURE_NAME=$(basename "$FILE" .md)
  SAFE_NAME=$(echo "$FEATURE_NAME" | tr ' /' '__')

  echo "===================================="
  echo "🚀 Processing $FEATURE_NAME"
  echo "===================================="

  FEATURE=$(cat "$FILE")
  BASE_COMMIT=$(git rev-parse HEAD)

  FEATURE_FAILED=false

  # =====================
  # DEV
  # =====================
  echo "👨‍💻 Running DEV..."

  DEV_PROMPT="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"$'\n\n'"$FEATURE"

  if ! codex exec "$DEV_PROMPT" > "workspace/logs/${SAFE_NAME}_dev.log" 2>&1; then
    echo "❌ DEV failed for $FEATURE_NAME"
    echo "- ❌ $FEATURE_NAME: DEV failed" >> "$SUMMARY_FILE"
    cp "workspace/logs/${SAFE_NAME}_dev.log" "workspace/failures/${SAFE_NAME}_dev_failed.log"
    continue
  fi

  git add .
  git commit -m "feat(ai): $FEATURE_NAME" || echo "⚠️ Nothing to commit after DEV"

  # =====================
  # DIFF
  # =====================
  git diff "$BASE_COMMIT" HEAD > "workspace/logs/${SAFE_NAME}_diff_full.txt"
  git diff --name-only "$BASE_COMMIT" HEAD > "workspace/logs/${SAFE_NAME}_files.txt"
  git diff "$BASE_COMMIT" HEAD | grep -E "^[+-]" | head -n 300 > "workspace/logs/${SAFE_NAME}_diff.txt" || true

  if [ ! -s "workspace/logs/${SAFE_NAME}_files.txt" ]; then
    echo "⚠️ No changes detected for $FEATURE_NAME"
    echo "- ⚠️ $FEATURE_NAME: no changes detected" >> "$SUMMARY_FILE"
    continue
  fi

  # =====================
  # TESTER
  # =====================
  echo "🧪 Running TESTER..."

  TESTER_INPUT="Analyze the following changes and update tests accordingly.

Changed files:
$(cat "workspace/logs/${SAFE_NAME}_files.txt")

Relevant diff:
$(cat "workspace/logs/${SAFE_NAME}_diff.txt")

Keep changes minimal. Do not rewrite unrelated tests."

  TESTER_PROMPT="$TESTER_SKILL"$'\n\n'"$TESTER_INPUT"

  if ! codex exec "$TESTER_PROMPT" > "workspace/logs/${SAFE_NAME}_tester.log" 2>&1; then
    echo "⚠️ TESTER failed for $FEATURE_NAME, continuing to validation"
    FEATURE_FAILED=true
  fi

  git add .
  git commit -m "test(ai): update tests for $FEATURE_NAME" || echo "⚠️ No test changes"

  # =====================
  # TEST + FIX LOOP
  # =====================
  echo "🧪 Running backend tests..."

  ATTEMPT=1
  TESTS_PASSED=false

  while [ "$ATTEMPT" -le "$MAX_FIX_ATTEMPTS" ]; do
    echo "➡️ Test attempt $ATTEMPT..."

    if (cd backend/farmapp && mvn test > "../../workspace/logs/${SAFE_NAME}_mvn_attempt_${ATTEMPT}.log" 2>&1); then
      echo "✅ Tests passed for $FEATURE_NAME"
      TESTS_PASSED=true
      break
    fi

    echo "❌ Tests failed for $FEATURE_NAME"

    grep -A 8 -B 8 "ERROR\|FAILURE\|Failures:" "workspace/logs/${SAFE_NAME}_mvn_attempt_${ATTEMPT}.log" > "workspace/logs/${SAFE_NAME}_error_focus.txt" || true

    echo "🛠 Running FIXER..."

    FIXER_INPUT="Fix ONLY the specific test or compilation failure below.

Feature:
$FEATURE_NAME

Relevant error:
$(cat "workspace/logs/${SAFE_NAME}_error_focus.txt")

Rules:
- Do NOT delete, disable, or skip tests
- Do NOT refactor unrelated code
- Keep the fix minimal
- If the issue is a test expectation mismatch, align the correct side with the existing system contract
- If unsure, make the smallest safe correction"

    FIXER_PROMPT="$FIXER_SKILL"$'\n\n'"$FIXER_INPUT"

    if ! codex exec "$FIXER_PROMPT" > "workspace/logs/${SAFE_NAME}_fixer_attempt_${ATTEMPT}.log" 2>&1; then
      echo "⚠️ FIXER failed on attempt $ATTEMPT"
    fi

    FILES_CHANGED=$(git diff --name-only | wc -l)
    CHANGED_LINES=$(git diff --numstat | awk '{ added += $1; deleted += $2 } END { print added + deleted + 0 }')

    if [ "$FILES_CHANGED" -gt 8 ] || [ "$CHANGED_LINES" -gt 500 ]; then
      echo "⚠️ Fix too large (${FILES_CHANGED} files, ${CHANGED_LINES} lines). Keeping changes but marking feature as failed."
      FEATURE_FAILED=true
      break
    fi

    git add .
    git commit -m "fix(ai): auto-fix $FEATURE_NAME attempt $ATTEMPT" || echo "⚠️ No fix changes"

    ATTEMPT=$((ATTEMPT + 1))
  done

  if [ "$TESTS_PASSED" = true ] && [ "$FEATURE_FAILED" = false ]; then
    echo "✅ Feature completed: $FEATURE_NAME"
    echo "- ✅ $FEATURE_NAME: completed" >> "$SUMMARY_FILE"
  else
    echo "❌ Feature failed but pipeline will continue: $FEATURE_NAME"
    echo "- ❌ $FEATURE_NAME: failed, check workspace/logs/${SAFE_NAME}_*" >> "$SUMMARY_FILE"
    cp "workspace/logs/${SAFE_NAME}_mvn_attempt_${ATTEMPT}.log" "workspace/failures/${SAFE_NAME}_failed.log" 2>/dev/null || true
  fi

done

echo "===================================="
echo "🎉 PIPELINE FINISHED"
echo "===================================="
cat "$SUMMARY_FILE"