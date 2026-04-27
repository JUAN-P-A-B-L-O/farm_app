#!/bin/bash

set -e

mkdir -p workspace

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "===================================="

# =====================
# 🔹 CONTEXT LOAD (1x)
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

  DEV_SYSTEM="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"
  DEV_PROMPT="$DEV_SYSTEM"$'\n\n'"$FEATURE"

  codex exec "$DEV_PROMPT"

  git add .
  git commit -m "feat(ai): $(basename "$FILE")" || echo "⚠️ Nothing to commit"

  # =====================
  # 📂 DIFF
  # =====================
  echo "📂 Generating diff..."

  git diff $BASE_COMMIT HEAD > workspace/diff_full.txt
  git diff --name-only $BASE_COMMIT HEAD > workspace/files.txt

  # diff reduzido (economia de token)
  git diff $BASE_COMMIT HEAD | grep -E "^[+-]" | head -n 300 > workspace/diff.txt || true

  if [ ! -s workspace/files.txt ]; then
    echo "⚠️ No changes detected, skipping..."
    continue
  fi

  # =====================
  # 🧪 TESTER
  # =====================
  echo "🧪 Running TESTER..."

  TESTER_SYSTEM="$TESTER_SKILL"

  TESTER_INPUT="Analyze the following changes and UPDATE tests accordingly.

Changed files:
$(cat workspace/files.txt)

Relevant diff:
$(cat workspace/diff.txt)

Ensure tests reflect the new behavior and remain aligned with the implementation."

  TESTER_PROMPT="$TESTER_SYSTEM"$'\n\n'"$TESTER_INPUT"

  codex exec "$TESTER_PROMPT"

  git add .
  git commit -m "test(ai): update tests for $(basename "$FILE")" || echo "⚠️ No test changes"

  # =====================
  # 🔁 LOOP DE TESTE + FIX
  # =====================
  echo "🧪 Running tests with auto-fix..."

  ATTEMPT=1
  SUCCESS=false

  while [ $ATTEMPT -le $MAX_FIX_ATTEMPTS ]; do

    echo "➡️ Attempt $ATTEMPT..."

    # =====================
    # 🧪 EXECUÇÃO REAL (CORRETO)
    # =====================
    if (cd backend/farmapp && mvn test > ../../workspace/mvn.log 2>&1); then
      echo "✅ Tests passed"
      SUCCESS=true
      break
    fi

    echo "❌ Tests failed"

    # =====================
    # 🔎 CAPTURA DE ERRO MELHOR
    # =====================
    grep -A 5 -B 5 "ERROR" workspace/mvn.log > workspace/error_focus.txt || true

    # =====================
    # 🛠 FIXER
    # =====================
    echo "🛠 Running FIXER..."

    FIXER_SYSTEM="$FIXER_SKILL"

    FIXER_INPUT="Fix the issues causing test or compilation failures.

Relevant error:
$(cat workspace/error_focus.txt)

Full tail:
$(tail -n 50 workspace/mvn.log)

Rules:
- Do NOT delete or disable tests
- Fix root cause, not symptoms
- Keep changes minimal
- Do NOT refactor unrelated code
"

    FIXER_PROMPT="$FIXER_SYSTEM"$'\n\n'"$FIXER_INPUT"

    codex exec "$FIXER_PROMPT"

    # =====================
    # 🔒 PROTEÇÃO CONTRA FIXES GRANDES
    # =====================
    CHANGED_LINES=$(git diff --stat | grep -Eo '[0-9]+ insertions' | grep -Eo '[0-9]+' || echo 0)

    if [ "$CHANGED_LINES" -gt 200 ]; then
      echo "⚠️ Fix too large ($CHANGED_LINES lines). Aborting to avoid damage."
      exit 1
    fi

    git add .
    git commit -m "fix(ai): auto-fix after test failure (attempt $ATTEMPT)" || echo "⚠️ No fix changes"

    ATTEMPT=$((ATTEMPT + 1))

  done

  if [ "$SUCCESS" = false ]; then
    echo "❌ Failed after $MAX_FIX_ATTEMPTS attempts"
    exit 1
  fi

  echo "✅ Feature completed successfully"

done

echo "===================================="
echo "🎉 ALL FEATURES COMPLETED"
echo "===================================="