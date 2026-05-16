#!/bin/bash

set -u

RUN_ID=$(date +"%Y%m%d_%H%M%S")
LOG_DIR="workspace/logs/$RUN_ID"
FAIL_DIR="workspace/failures/$RUN_ID"
SUMMARY_FILE="workspace/pipeline_summary.md"

FEATURES_DIR="workspace/features"
DONE_DIR="workspace/done"
FAILED_FEATURES_DIR="workspace/failed_features"
PLANS_DIR="workspace/plans"

mkdir -p "$LOG_DIR" "$FAIL_DIR" "$DONE_DIR" "$FAILED_FEATURES_DIR" "$PLANS_DIR"

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "Run ID: $RUN_ID"
echo "===================================="

# =====================
# CONTEXT / SKILLS
# =====================
CONTEXT_FILE="contextAi.md"
if [ ! -f "$CONTEXT_FILE" ]; then
  CONTEXT_FILE="AI_CONTEXT.md"
fi

AI_CONTEXT=$(cat "$CONTEXT_FILE")
DEV_SKILL=$(cat skills/dev.md)
TESTER_SKILL=$(cat skills/tester.md)
FIXER_SKILL=$(cat skills/fixer.md)

if [ -f "skills/planner.md" ]; then
  PLANNER_SKILL=$(cat skills/planner.md)
else
  PLANNER_SKILL="You are a senior software planner. Create a concise implementation plan. Do not write code. Focus on target areas, steps, risks, and validation."
fi

# =====================
# CONFIG
# =====================
ENABLE_PLANNER=false

PLANNER_EFFORT="medium"
DEV_EFFORT="high"
TESTER_EFFORT="low"
FIXER_EFFORT="high"

MAX_FIX_ATTEMPTS=1
DIFF_LINES=80
ERROR_LINES_AROUND=8

MAX_CHANGED_FILES_ABORT=25
MAX_CHANGED_LINES_ABORT=2500

BACKEND_DIR="backend/farmapp"

# =====================
# HELPERS
# =====================
RUN_CODEX() {
  local EFFORT="$1"
  local PROMPT="$2"
  local LOG_FILE="$3"

  codex exec -c reasoning_effort="$EFFORT" "$PROMPT" > "$LOG_FILE" 2>&1
}

GIT_ADD_SAFE() {
  git add -A -- .
  git reset -q -- workspace 2>/dev/null || true
}

COMMIT_IF_HAS_CHANGES() {
  local MESSAGE="$1"

  GIT_ADD_SAFE

  if git diff --cached --quiet; then
    echo "⚠️ Nothing to commit: $MESSAGE"
    return 1
  fi

  git commit -m "$MESSAGE"
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

MOVE_FEATURE_TO_FAILED() {
  local FILE="$1"
  local BASENAME
  local TARGET

  [ -f "$FILE" ] || return 0

  BASENAME=$(basename "$FILE")
  TARGET="$FAILED_FEATURES_DIR/$BASENAME"

  if [ -e "$TARGET" ]; then
    TARGET="$FAILED_FEATURES_DIR/${RUN_ID}_$BASENAME"
  fi

  mv "$FILE" "$TARGET"
  echo "📦 Moved failed feature file to $TARGET"
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

CHECK_ABSURD_CHANGES_ABORT() {
  local FILES_CHANGED
  local CHANGED_LINES

  FILES_CHANGED=$(git diff --name-only -- . ':!workspace/**' | wc -l)
  CHANGED_LINES=$(git diff --numstat -- . ':!workspace/**' | awk '{ added += $1; deleted += $2 } END { print added + deleted + 0 }')

  if [ "$FILES_CHANGED" -gt "$MAX_CHANGED_FILES_ABORT" ] || [ "$CHANGED_LINES" -gt "$MAX_CHANGED_LINES_ABORT" ]; then
    echo "🛑 Absurdly large changes detected: ${FILES_CHANGED} files, ${CHANGED_LINES} lines."
    echo "- 🛑 Pipeline aborted: absurd changes (${FILES_CHANGED} files, ${CHANGED_LINES} lines)" >> "$SUMMARY_FILE"
    exit 1
  fi
}

SHOW_LOG_TAIL() {
  local LABEL="$1"
  local LOG_FILE="$2"

  echo "------ $LABEL LOG TAIL ------"
  tail -n 80 "$LOG_FILE" 2>/dev/null || true
  echo "-----------------------------"
}

# =====================
# SUMMARY INIT
# =====================
echo "# AI Pipeline Summary" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "- Run ID: $RUN_ID" >> "$SUMMARY_FILE"
echo "- Context file: $CONTEXT_FILE" >> "$SUMMARY_FILE"
echo "- Planner enabled: $ENABLE_PLANNER" >> "$SUMMARY_FILE"
echo "- Planner effort: $PLANNER_EFFORT" >> "$SUMMARY_FILE"
echo "- Dev effort: $DEV_EFFORT" >> "$SUMMARY_FILE"
echo "- Tester effort: $TESTER_EFFORT" >> "$SUMMARY_FILE"
echo "- Fixer effort: $FIXER_EFFORT" >> "$SUMMARY_FILE"
echo "- Max fix attempts: $MAX_FIX_ATTEMPTS" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

mapfile -t FEATURE_FILES < <(find "$FEATURES_DIR" -maxdepth 1 -type f -name "*.md" | sort -V)

if [ "${#FEATURE_FILES[@]}" -eq 0 ]; then
  echo "⚠️ No feature files found in $FEATURES_DIR"
fi

# =====================
# FEATURE LOOP
# =====================
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
  FIX_ATTEMPT=0

  PLAN_FILE="$PLANS_DIR/${SAFE_NAME}.plan.md"
  PLAN_CONTENT=""

  # =====================
  # OPTIONAL PLANNER
  # =====================
  if [ "$ENABLE_PLANNER" = true ]; then
    echo "🧠 Running PLANNER..."

    PLANNER_INPUT="Create a concise implementation plan for the feature below.

Feature:
$FEATURE

Rules:
- Do not write code.
- Keep the plan concise.
- Identify target files/modules if possible.
- Identify risks.
- Identify validation steps.
- Prefer incremental execution.
- Do not modify files.
- Do not touch workspace files."

    PLANNER_PROMPT="$AI_CONTEXT"$'\n\n'"$PLANNER_SKILL"$'\n\n'"$PLANNER_INPUT"
    PLANNER_LOG="$LOG_DIR/${SAFE_NAME}_planner.log"

    if RUN_CODEX "$PLANNER_EFFORT" "$PLANNER_PROMPT" "$PLANNER_LOG"; then
      cp "$PLANNER_LOG" "$PLAN_FILE" 2>/dev/null || true
      PLAN_CONTENT=$(cat "$PLAN_FILE")
      echo "✅ Planner completed"
      echo "- 🧠 $FEATURE_NAME: planner used" >> "$SUMMARY_FILE"
    else
      SHOW_LOG_TAIL "PLANNER" "$PLANNER_LOG"

      if IS_HARD_TOKEN_LIMIT_ERROR "$PLANNER_LOG"; then
        echo "⏸️ Planner hit token/rate limit for $FEATURE_NAME. Continuing without plan."
        echo "- ⏸️ $FEATURE_NAME: planner token/rate limit, continued without plan" >> "$SUMMARY_FILE"
      else
        echo "⚠️ Planner failed for $FEATURE_NAME. Continuing without plan."
        echo "- ⚠️ $FEATURE_NAME: planner failed, continued without plan" >> "$SUMMARY_FILE"
      fi

      PLAN_CONTENT=""
    fi
  else
    echo "⏭️ Planner skipped for $FEATURE_NAME"
    echo "- ⏭️ $FEATURE_NAME: planner skipped" >> "$SUMMARY_FILE"
  fi

  # =====================
  # DEV
  # =====================
  echo "👨‍💻 Running DEV..."

  if [ -n "$PLAN_CONTENT" ]; then
    DEV_INPUT="Feature:
$FEATURE

Implementation plan:
$PLAN_CONTENT"
  else
    DEV_INPUT="$FEATURE"
  fi

  DEV_PROMPT="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"$'\n\n'"$DEV_INPUT"$'\n\n'"Hard rules:
- Do not modify files under workspace/.
- Do not move feature files.
- Do not edit pipeline files unless explicitly requested.
- Implement only the requested feature in the application source code."

  DEV_LOG="$LOG_DIR/${SAFE_NAME}_dev.log"

  if ! RUN_CODEX "$DEV_EFFORT" "$DEV_PROMPT" "$DEV_LOG"; then
    SHOW_LOG_TAIL "DEV" "$DEV_LOG"

    if IS_HARD_TOKEN_LIMIT_ERROR "$DEV_LOG"; then
      echo "⏸️ DEV hit token/rate limit for $FEATURE_NAME. Skipping feature and continuing."
      echo "- ⏸️ $FEATURE_NAME: DEV token/rate limit" >> "$SUMMARY_FILE"
    else
      echo "❌ DEV failed for $FEATURE_NAME. Continuing next feature."
      echo "- ❌ $FEATURE_NAME: DEV failed" >> "$SUMMARY_FILE"
    fi

    cp "$DEV_LOG" "$FAIL_DIR/${SAFE_NAME}_dev_failed.log" 2>/dev/null || true
    MOVE_FEATURE_TO_FAILED "$FILE"
    continue
  fi

  CHECK_ABSURD_CHANGES_ABORT

  COMMIT_IF_HAS_CHANGES "feat(ai): $FEATURE_NAME" || true

  # =====================
  # DIFF
  # =====================
  git diff "$BASE_COMMIT" HEAD -- . ':!workspace/**' > "$LOG_DIR/${SAFE_NAME}_diff_full.txt"
  git diff --name-only "$BASE_COMMIT" HEAD -- . ':!workspace/**' > "$LOG_DIR/${SAFE_NAME}_files.txt"
  git diff "$BASE_COMMIT" HEAD -- . ':!workspace/**' | grep -E "^[+-]" | head -n "$DIFF_LINES" > "$LOG_DIR/${SAFE_NAME}_diff.txt" || true

  if [ ! -s "$LOG_DIR/${SAFE_NAME}_files.txt" ]; then
    echo "⚠️ No application changes detected for $FEATURE_NAME"
    echo "- ⚠️ $FEATURE_NAME: no application changes detected" >> "$SUMMARY_FILE"
    MOVE_FEATURE_TO_FAILED "$FILE"
    continue
  fi

  # =====================
  # TESTER
  # =====================
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
- Keep test changes minimal.
- Do not modify files under workspace/."

  TESTER_PROMPT="$TESTER_SKILL"$'\n\n'"$TESTER_INPUT"
  TESTER_LOG="$LOG_DIR/${SAFE_NAME}_tester.log"

  if ! RUN_CODEX "$TESTER_EFFORT" "$TESTER_PROMPT" "$TESTER_LOG"; then
    SHOW_LOG_TAIL "TESTER" "$TESTER_LOG"

    if IS_HARD_TOKEN_LIMIT_ERROR "$TESTER_LOG"; then
      echo "⏸️ TESTER hit token/rate limit for $FEATURE_NAME. Continuing to validation."
      echo "- ⏸️ $FEATURE_NAME: TESTER token/rate limit" >> "$SUMMARY_FILE"
    else
      echo "⚠️ TESTER failed for $FEATURE_NAME. Continuing to validation."
      echo "- ⚠️ $FEATURE_NAME: TESTER failed, continued to validation" >> "$SUMMARY_FILE"
    fi

    FEATURE_FAILED=true
  else
    CHECK_ABSURD_CHANGES_ABORT
    COMMIT_IF_HAS_CHANGES "test(ai): update tests for $FEATURE_NAME" || true
  fi

  # =====================
  # TEST + FIX LOOP
  # =====================
  echo "🧪 Running backend tests..."

  while true; do
    TEST_ATTEMPT=$((FIX_ATTEMPT + 1))
    echo "➡️ Test attempt $TEST_ATTEMPT..."

    MVN_LOG="$LOG_DIR/${SAFE_NAME}_mvn_attempt_${TEST_ATTEMPT}.log"

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
    SHOW_LOG_TAIL "MAVEN" "$MVN_LOG"

    if [ "$FIX_ATTEMPT" -ge "$MAX_FIX_ATTEMPTS" ]; then
      echo "❌ Max fix attempts reached for $FEATURE_NAME"
      break
    fi

    FIX_ATTEMPT=$((FIX_ATTEMPT + 1))

    ERROR_FOCUS="$LOG_DIR/${SAFE_NAME}_error_focus_attempt_${FIX_ATTEMPT}.txt"

    grep -A "$ERROR_LINES_AROUND" -B "$ERROR_LINES_AROUND" \
      "ERROR\|FAILURE\|Failures:\|Errors:\|expected:<.*> but was:<.*>\|method does not override\|cannot find symbol\|COMPILATION ERROR" \
      "$MVN_LOG" > "$ERROR_FOCUS" || true

    if [ ! -s "$ERROR_FOCUS" ]; then
      tail -n 120 "$MVN_LOG" > "$ERROR_FOCUS" || true
    fi

    echo "🛠 Running FIXER attempt $FIX_ATTEMPT..."

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
- If unsure, make the smallest safe correction.
- Do not modify files under workspace/."

    FIXER_PROMPT="$FIXER_SKILL"$'\n\n'"$FIXER_INPUT"
    FIXER_LOG="$LOG_DIR/${SAFE_NAME}_fixer_attempt_${FIX_ATTEMPT}.log"

    if ! RUN_CODEX "$FIXER_EFFORT" "$FIXER_PROMPT" "$FIXER_LOG"; then
      SHOW_LOG_TAIL "FIXER" "$FIXER_LOG"

      if IS_HARD_TOKEN_LIMIT_ERROR "$FIXER_LOG"; then
        echo "⏸️ FIXER hit token/rate limit for $FEATURE_NAME. Marking feature failed and continuing."
        echo "- ⏸️ $FEATURE_NAME: FIXER token/rate limit" >> "$SUMMARY_FILE"
      else
        echo "⚠️ FIXER failed on attempt $FIX_ATTEMPT"
        echo "- ⚠️ $FEATURE_NAME: FIXER failed on attempt $FIX_ATTEMPT" >> "$SUMMARY_FILE"
      fi

      FEATURE_FAILED=true
      break
    fi

    CHECK_ABSURD_CHANGES_ABORT
    COMMIT_IF_HAS_CHANGES "fix(ai): auto-fix $FEATURE_NAME attempt $FIX_ATTEMPT" || true

    # Important:
    # After FIXER, the loop continues and runs mvn test again.
  done

  # =====================
  # FEATURE RESULT
  # =====================
  if [ "$TESTS_PASSED" = true ] && [ "$FEATURE_FAILED" = false ]; then
    echo "✅ Feature completed: $FEATURE_NAME"
    echo "- ✅ $FEATURE_NAME: completed" >> "$SUMMARY_FILE"
    MOVE_FEATURE_TO_DONE "$FILE"
  elif [ "$TESTS_PASSED" = true ]; then
    echo "⚠️ Feature partially completed: $FEATURE_NAME"
    echo "- ⚠️ $FEATURE_NAME: tests passed, but one agent failed/limited" >> "$SUMMARY_FILE"
    MOVE_FEATURE_TO_DONE "$FILE"
  else
    echo "❌ Feature failed but pipeline will continue: $FEATURE_NAME"
    echo "- ❌ $FEATURE_NAME: failed, check $LOG_DIR/${SAFE_NAME}_*" >> "$SUMMARY_FILE"

    LAST_MVN_LOG="$LOG_DIR/${SAFE_NAME}_mvn_attempt_$((FIX_ATTEMPT + 1)).log"
    cp "$LAST_MVN_LOG" "$FAIL_DIR/${SAFE_NAME}_failed.log" 2>/dev/null || true

    MOVE_FEATURE_TO_FAILED "$FILE"
  fi

  FEATURE_TOKENS=$(TOKEN_COUNT "$LOG_DIR/${SAFE_NAME}_*.log")

  echo "🔢 Tokens for $FEATURE_NAME: $FEATURE_TOKENS"
  echo "  - Tokens: $FEATURE_TOKENS" >> "$SUMMARY_FILE"

done

# =====================
# FINAL SUMMARY
# =====================
echo "===================================="
echo "🎉 PIPELINE FINISHED"
echo "===================================="
cat "$SUMMARY_FILE"

echo ""
echo "===================================="
echo "📊 TOKEN USAGE SUMMARY"
echo "===================================="

TOTAL_TOKENS=$(TOKEN_COUNT "$LOG_DIR/*.log")

FEATURES_PROCESSED=$(grep -cE "completed|partially completed|failed|token/rate limit|no application changes detected|DEV failed" "$SUMMARY_FILE")
AVG_TOKENS=0

if [ "$FEATURES_PROCESSED" -gt 0 ]; then
  AVG_TOKENS=$((TOTAL_TOKENS / FEATURES_PROCESSED))
fi

echo "Total tokens used: $TOTAL_TOKENS"
echo "Features processed: $FEATURES_PROCESSED"
echo "Average tokens per feature: $AVG_TOKENS"

if [ "$AVG_TOKENS" -lt 50000 ]; then
  COST_LEVEL="LOW"
elif [ "$AVG_TOKENS" -lt 120000 ]; then
  COST_LEVEL="MEDIUM"
else
  COST_LEVEL="HIGH"
fi

echo "Cost level: $COST_LEVEL"

echo "" >> "$SUMMARY_FILE"
echo "## Token Usage" >> "$SUMMARY_FILE"
echo "- Total tokens used: $TOTAL_TOKENS" >> "$SUMMARY_FILE"
echo "- Features processed: $FEATURES_PROCESSED" >> "$SUMMARY_FILE"
echo "- Average tokens per feature: $AVG_TOKENS" >> "$SUMMARY_FILE"
echo "- Cost level: $COST_LEVEL" >> "$SUMMARY_FILE"