#!/bin/bash

set -u

RUN_ID=$(date +"%Y%m%d_%H%M%S")
LOG_DIR="workspace/logs/$RUN_ID"
FAIL_DIR="workspace/failures/$RUN_ID"
SUMMARY_FILE="workspace/pipeline_summary.md"

FEATURES_DIR="workspace/features"
DONE_DIR="workspace/done"

mkdir -p "$LOG_DIR" "$FAIL_DIR" "$DONE_DIR"

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "Run ID: $RUN_ID"
echo "===================================="

CONTEXT_FILE="contextAi.md"
if [ ! -f "$CONTEXT_FILE" ]; then
  CONTEXT_FILE="AI_CONTEXT.md"
fi

AI_CONTEXT=$(cat "$CONTEXT_FILE")
DEV_SKILL=$(cat skills/dev.md)
TESTER_SKILL=$(cat skills/tester.md)
FIXER_SKILL=$(cat skills/fixer.md)

MAX_FIX_ATTEMPTS=1
DIFF_LINES=100
ERROR_LINES_AROUND=5

MAX_CHANGED_FILES_ABORT=25
MAX_CHANGED_LINES_ABORT=2500
MAX_TOTAL_TOKENS_ABORT=600000

BACKEND_DIR="backend/farmapp"

GIT_ADD_SAFE() {
  git add -A . \
    ':!workspace/logs/**' \
    ':!workspace/failures/**' \
    ':!workspace/pipeline_summary.md'
}

MOVE_FEATURE_TO_DONE() {
  local FILE="$1"
  local BASENAME
  local TARGET

  [ -f "$FILE" ] || return 0

  BASENAME=$(basename "$FILE")
  TARGET="$DONE_DIR/$BASENAME"

  if [ -e "$TARGET" ]; then
    TARGET="$DONE_DIR/${RUN_ID}_$BASENAME"
  fi

  mv "$FILE" "$TARGET"
  echo "📦 Moved feature file to $TARGET"
}

IS_HARD_TOKEN_LIMIT_ERROR() {
  local LOG_FILE="$1"

  grep -qiE \
    "rate limit exceeded|usage limit reached|quota exceeded|insufficient quota|you have reached your usage limit|request limit exceeded|too many requests" \
    "$LOG_FILE"
}

TOKEN_COUNT() {
  local PATTERN="$1"

  awk '
    /tokens used/ {
      getline
      gsub(",", "", $1)
      sum += $1
    }
    END { print sum + 0 }
  ' $PATTERN 2>/dev/null
}

CHECK_TOTAL_TOKENS_ABORT() {
  local TOTAL
  TOTAL=$(TOKEN_COUNT "$LOG_DIR/*.log")

  if [ "$TOTAL" -gt "$MAX_TOTAL_TOKENS_ABORT" ]; then
    echo "🛑 Total token usage too high: $TOTAL. Aborting pipeline."
    echo "- 🛑 Pipeline aborted: token usage too high ($TOTAL)" >> "$SUMMARY_FILE"
    exit 1
  fi
}

CHECK_ABSURD_CHANGES_ABORT() {
  local FILES_CHANGED
  local CHANGED_LINES

  FILES_CHANGED=$(git diff --name-only | grep -v '^workspace/' | wc -l)
  CHANGED_LINES=$(git diff --numstat -- . ':!workspace' | awk '{ added += $1; deleted += $2 } END { print added + deleted + 0 }')

  if [ "$FILES_CHANGED" -gt "$MAX_CHANGED_FILES_ABORT" ] || [ "$CHANGED_LINES" -gt "$MAX_CHANGED_LINES_ABORT" ]; then
    echo "🛑 Absurdly large changes detected: ${FILES_CHANGED} files, ${CHANGED_LINES} lines."
    echo "- 🛑 Pipeline aborted: absurd changes (${FILES_CHANGED} files, ${CHANGED_LINES} lines)" >> "$SUMMARY_FILE"
    exit 1
  fi
}

echo "# AI Pipeline Summary" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "- Run ID: $RUN_ID" >> "$SUMMARY_FILE"
echo "- Context file: $CONTEXT_FILE" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

mapfile -t FEATURE_FILES < <(find "$FEATURES_DIR" -maxdepth 1 -type f -name "*.md" | sort -V)

if [ "${#FEATURE_FILES[@]}" -eq 0 ]; then
  echo "⚠️ No feature files found in $FEATURES_DIR"
fi

for FILE in "${FEATURE_FILES[@]}"; do
  FEATURE_NAME=$(basename "$FILE" .md)
  SAFE_NAME=$(echo "$FEATURE_NAME" | tr ' /' '__')

  echo "===================================="
  echo "🚀 Processing $FEATURE_NAME"
  echo "===================================="

  FEATURE=$(cat "$FILE")
  BASE_COMMIT=$(git rev-parse HEAD)

  FEATURE_FAILED=false
  TESTS_PASSED=false

  echo "👨‍💻 Running DEV..."

  DEV_PROMPT="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"$'\n\n'"$FEATURE"
  DEV_LOG="$LOG_DIR/${SAFE_NAME}_dev.log"

  if ! codex exec "$DEV_PROMPT" > "$DEV_LOG" 2>&1; then
    if IS_HARD_TOKEN_LIMIT_ERROR "$DEV_LOG"; then
      echo "⏸️ DEV hit token/rate limit for $FEATURE_NAME. Skipping feature and continuing."
      echo "- ⏸️ $FEATURE_NAME: DEV token/rate limit" >> "$SUMMARY_FILE"
    else
      echo "❌ DEV failed for $FEATURE_NAME. Continuing next feature."
      echo "- ❌ $FEATURE_NAME: DEV failed" >> "$SUMMARY_FILE"
    fi

    cp "$DEV_LOG" "$FAIL_DIR/${SAFE_NAME}_dev_failed.log" 2>/dev/null || true
    CHECK_TOTAL_TOKENS_ABORT
    MOVE_FEATURE_TO_DONE "$FILE"
    continue
  fi

  CHECK_TOTAL_TOKENS_ABORT
  CHECK_ABSURD_CHANGES_ABORT

  GIT_ADD_SAFE
  git commit -m "feat(ai): $FEATURE_NAME" || echo "⚠️ Nothing to commit after DEV"

  git diff "$BASE_COMMIT" HEAD > "$LOG_DIR/${SAFE_NAME}_diff_full.txt"
  git diff --name-only "$BASE_COMMIT" HEAD > "$LOG_DIR/${SAFE_NAME}_files.txt"
  git diff "$BASE_COMMIT" HEAD | grep -E "^[+-]" | head -n "$DIFF_LINES" > "$LOG_DIR/${SAFE_NAME}_diff.txt" || true

  if [ ! -s "$LOG_DIR/${SAFE_NAME}_files.txt" ]; then
    echo "⚠️ No changes detected for $FEATURE_NAME"
    echo "- ⚠️ $FEATURE_NAME: no changes detected" >> "$SUMMARY_FILE"
    MOVE_FEATURE_TO_DONE "$FILE"
    continue
  fi

  echo "🧪 Running TESTER..."

  TESTER_INPUT="Update or add tests only for the changed behavior.

Changed files:
$(cat "$LOG_DIR/${SAFE_NAME}_files.txt")

Diff excerpt:
$(cat "$LOG_DIR/${SAFE_NAME}_diff.txt")

Rules:
- Focus only on changed files and behavior.
- Do not rewrite unrelated tests.
- Do not add broad test suites.
- Keep test changes minimal."

  TESTER_PROMPT="$TESTER_SKILL"$'\n\n'"$TESTER_INPUT"
  TESTER_LOG="$LOG_DIR/${SAFE_NAME}_tester.log"

  if ! codex exec "$TESTER_PROMPT" > "$TESTER_LOG" 2>&1; then
    if IS_HARD_TOKEN_LIMIT_ERROR "$TESTER_LOG"; then
      echo "⏸️ TESTER hit token/rate limit for $FEATURE_NAME. Continuing to validation."
      echo "- ⏸️ $FEATURE_NAME: TESTER token/rate limit" >> "$SUMMARY_FILE"
    else
      echo "⚠️ TESTER failed for $FEATURE_NAME. Continuing to validation."
    fi

    FEATURE_FAILED=true
  else
    CHECK_ABSURD_CHANGES_ABORT
    GIT_ADD_SAFE
    git commit -m "test(ai): update tests for $FEATURE_NAME" || echo "⚠️ No test changes"
  fi

  CHECK_TOTAL_TOKENS_ABORT

  echo "🧪 Running backend tests..."

  ATTEMPT=1

  while [ "$ATTEMPT" -le "$MAX_FIX_ATTEMPTS" ]; do
    echo "➡️ Test attempt $ATTEMPT..."

    MVN_LOG="$LOG_DIR/${SAFE_NAME}_mvn_attempt_${ATTEMPT}.log"

    if [ ! -d "$BACKEND_DIR" ]; then
      echo "⚠️ Backend directory not found: $BACKEND_DIR. Skipping backend tests."
      echo "- ⚠️ $FEATURE_NAME: backend tests skipped, directory not found" >> "$SUMMARY_FILE"
      TESTS_PASSED=true
      break
    fi

    if (cd "$BACKEND_DIR" && mvn test > "../../$MVN_LOG" 2>&1); then
      echo "✅ Tests passed for $FEATURE_NAME"
      TESTS_PASSED=true
      break
    fi

    echo "❌ Tests failed for $FEATURE_NAME"

    ERROR_FOCUS="$LOG_DIR/${SAFE_NAME}_error_focus.txt"

    grep -A "$ERROR_LINES_AROUND" -B "$ERROR_LINES_AROUND" \
      "ERROR\|FAILURE\|Failures:\|expected:<.*> but was:<.*>\|method does not override" \
      "$MVN_LOG" > "$ERROR_FOCUS" || true

    echo "🛠 Running FIXER..."

    FIXER_INPUT="Fix only the specific test or compilation failure below.

Feature:
$FEATURE_NAME

Relevant error:
$(cat "$ERROR_FOCUS")

Rules:
- Do not delete, disable, or skip tests.
- Do not refactor unrelated code.
- Keep the fix minimal.
- If this is a test expectation mismatch, align the correct side with the existing system contract.
- If unsure, make the smallest safe correction."

    FIXER_PROMPT="$FIXER_SKILL"$'\n\n'"$FIXER_INPUT"
    FIXER_LOG="$LOG_DIR/${SAFE_NAME}_fixer_attempt_${ATTEMPT}.log"

    if ! codex exec "$FIXER_PROMPT" > "$FIXER_LOG" 2>&1; then
      if IS_HARD_TOKEN_LIMIT_ERROR "$FIXER_LOG"; then
        echo "⏸️ FIXER hit token/rate limit for $FEATURE_NAME. Marking feature failed and continuing."
        echo "- ⏸️ $FEATURE_NAME: FIXER token/rate limit" >> "$SUMMARY_FILE"
      else
        echo "⚠️ FIXER failed on attempt $ATTEMPT"
      fi

      FEATURE_FAILED=true
      break
    fi

    CHECK_TOTAL_TOKENS_ABORT
    CHECK_ABSURD_CHANGES_ABORT

    GIT_ADD_SAFE
    git commit -m "fix(ai): auto-fix $FEATURE_NAME attempt $ATTEMPT" || echo "⚠️ No fix changes"

    ATTEMPT=$((ATTEMPT + 1))
  done

  if [ "$TESTS_PASSED" = true ] && [ "$FEATURE_FAILED" = false ]; then
    echo "✅ Feature completed: $FEATURE_NAME"
    echo "- ✅ $FEATURE_NAME: completed" >> "$SUMMARY_FILE"
  elif [ "$TESTS_PASSED" = true ]; then
    echo "⚠️ Feature partially completed: $FEATURE_NAME"
    echo "- ⚠️ $FEATURE_NAME: tests passed, but one agent failed/limited" >> "$SUMMARY_FILE"
  else
    echo "❌ Feature failed but pipeline will continue: $FEATURE_NAME"
    echo "- ❌ $FEATURE_NAME: failed, check $LOG_DIR/${SAFE_NAME}_*" >> "$SUMMARY_FILE"
    cp "$LOG_DIR/${SAFE_NAME}_mvn_attempt_${ATTEMPT}.log" "$FAIL_DIR/${SAFE_NAME}_failed.log" 2>/dev/null || true
  fi

  FEATURE_TOKENS=$(TOKEN_COUNT "$LOG_DIR/${SAFE_NAME}_*.log")

  echo "🔢 Tokens for $FEATURE_NAME: $FEATURE_TOKENS"
  echo "  - Tokens: $FEATURE_TOKENS" >> "$SUMMARY_FILE"

  MOVE_FEATURE_TO_DONE "$FILE"

done

echo "===================================="
echo "🎉 PIPELINE FINISHED"
echo "===================================="
cat "$SUMMARY_FILE"

echo ""
echo "===================================="
echo "📊 TOKEN USAGE SUMMARY"
echo "===================================="

TOTAL_TOKENS=$(TOKEN_COUNT "$LOG_DIR/*.log")

echo "Total tokens used: $TOTAL_TOKENS"

if [ "$TOTAL_TOKENS" -lt 50000 ]; then
  COST_LEVEL="LOW"
elif [ "$TOTAL_TOKENS" -lt 150000 ]; then
  COST_LEVEL="MEDIUM"
else
  COST_LEVEL="HIGH"
fi

echo "Cost level: $COST_LEVEL"

echo "" >> "$SUMMARY_FILE"
echo "## Token Usage" >> "$SUMMARY_FILE"
echo "- Total tokens used: $TOTAL_TOKENS" >> "$SUMMARY_FILE"
echo "- Cost level: $COST_LEVEL" >> "$SUMMARY_FILE"