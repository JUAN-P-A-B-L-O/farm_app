#!/bin/bash

set -e

mkdir -p workspace

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "===================================="

# =====================
# 🔹 CONTEXT LOAD
# =====================
AI_CONTEXT=$(cat AI_CONTEXT.md)
DEV_SKILL=$(cat skills/dev.md)
TESTER_SKILL=$(cat skills/tester.md)
FIXER_SKILL=$(cat skills/fixer.md)

MAX_FIX_ATTEMPTS=3

# =====================
# 🔁 LOOP DE FEATURES
# =====================
for FILE in workspace/features/*.md; do

  echo "===================================="
  echo "🚀 Processing $(basename "$FILE")"
  echo "===================================="

  FEATURE=$(cat "$FILE")
  BASE_COMMIT=$(git rev-parse HEAD)

  # =====================
  # 👨‍💻 DEV
  # =====================
  echo "👨‍💻 Running DEV..."

  DEV_PROMPT="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"$'\n\n'"$FEATURE"
  codex exec "$DEV_PROMPT"

  git add .
  git commit -m "feat(ai): $(basename "$FILE")" || echo "⚠️ Nothing to commit"

  # =====================
  # 📂 DIFF
  # =====================
  git diff $BASE_COMMIT HEAD > workspace/diff_full.txt
  git diff --name-only $BASE_COMMIT HEAD > workspace/files.txt
  git diff $BASE_COMMIT HEAD | grep -E "^[+-]" | head -n 300 > workspace/diff.txt || true

  if [ ! -s workspace/files.txt ]; then
    echo "⚠️ No changes detected, skipping..."
    continue
  fi

  # =====================
  # 🧪 TESTER
  # =====================
  echo "🧪 Running TESTER..."

  TESTER_INPUT="Analyze changes and update tests.

Files:
$(cat workspace/files.txt)

Diff:
$(cat workspace/diff.txt)
"

  TESTER_PROMPT="$TESTER_SKILL"$'\n\n'"$TESTER_INPUT"
  codex exec "$TESTER_PROMPT"

  git add .
  git commit -m "test(ai): update tests for $(basename "$FILE")" || true

  # =====================
  # 🔁 TEST + CLASSIFY + FIX
  # =====================
  ATTEMPT=1
  SUCCESS=false

  while [ $ATTEMPT -le $MAX_FIX_ATTEMPTS ]; do

    echo "➡️ Attempt $ATTEMPT..."

    if (cd backend/farmapp && mvn test > ../../workspace/mvn.log 2>&1); then
      echo "✅ Tests passed"
      SUCCESS=true
      break
    fi

    echo "❌ Tests failed"

    # =====================
    # 🔎 ERROR CLASSIFIER
    # =====================
    ERROR_TYPE="unknown"

    if grep -q "expected:<.*> but was:<.*>" workspace/mvn.log; then
      ERROR_TYPE="contract"
    elif grep -q "method does not override" workspace/mvn.log; then
      ERROR_TYPE="signature"
    elif grep -q "NullPointerException" workspace/mvn.log; then
      ERROR_TYPE="runtime"
    fi

    echo "🔍 Error type: $ERROR_TYPE"

    grep -A 5 -B 5 "ERROR" workspace/mvn.log > workspace/error_focus.txt || true

    # =====================
    # 🛠 FIXER INPUT DINÂMICO
    # =====================
    case $ERROR_TYPE in

      contract)
        FIXER_INPUT="Fix contract mismatch.

Tests and implementation disagree.

Example:
$(cat workspace/error_focus.txt)

Rules:
- Prefer aligning backend responses
- Do NOT refactor unrelated code
- Minimal fix only
"
        ;;

      signature)
        FIXER_INPUT="Fix method signature mismatch.

$(cat workspace/error_focus.txt)

Rules:
- Align tests and implementation
- Fix @Override issues
- Do NOT refactor unrelated code
"
        ;;

      runtime)
        FIXER_INPUT="Fix runtime error.

$(cat workspace/error_focus.txt)

Rules:
- Identify root cause
- Fix minimal logic
"
        ;;

      *)
        FIXER_INPUT="Fix failure based on error:

$(tail -n 50 workspace/mvn.log)

Rules:
- Minimal fix
- No large refactors
"
        ;;
    esac

    echo "🛠 Running FIXER..."

    FIXER_PROMPT="$FIXER_SKILL"$'\n\n'"$FIXER_INPUT"
    codex exec "$FIXER_PROMPT"

    # =====================
    # 🔒 PROTEÇÃO
    # =====================
    CHANGED_LINES=$(git diff --stat | grep -Eo '[0-9]+ insertions' | grep -Eo '[0-9]+' || echo 0)

    if [ "$CHANGED_LINES" -gt 200 ]; then
      echo "⚠️ Fix too large ($CHANGED_LINES lines). Aborting."
      exit 1
    fi

    git add .
    git commit -m "fix(ai): attempt $ATTEMPT ($ERROR_TYPE)" || true

    ATTEMPT=$((ATTEMPT + 1))

  done

  if [ "$SUCCESS" = false ]; then
    echo "❌ Failed after $MAX_FIX_ATTEMPTS attempts"
    exit 1
  fi

  echo "✅ Feature completed"

done

echo "===================================="
echo "🎉 ALL FEATURES COMPLETED"
echo "===================================="