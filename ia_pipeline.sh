#!/bin/bash

set -e

mkdir -p workspace

echo "===================================="
echo "🚀 AI PIPELINE START"
echo "===================================="

# =====================
# 🔹 CONTEXT LOAD (1x só)
# =====================
AI_CONTEXT=$(cat AI_CONTEXT.md)
DEV_SKILL=$(cat skills/dev.md)
TESTER_SKILL=$(cat skills/tester.md)

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
  # 👨‍💻 DEV (contexto completo)
  # =====================
  echo "👨‍💻 Running DEV..."

  DEV_SYSTEM="$AI_CONTEXT"$'\n\n'"$DEV_SKILL"
  DEV_PROMPT="$DEV_SYSTEM"$'\n\n'"$FEATURE"

  echo "$DEV_PROMPT"

  codex exec "$DEV_PROMPT"

  # =====================
  # 💾 COMMIT (isolamento)
  # =====================
  git add .
  git commit -m "feat(ai): $(basename "$FILE")" || echo "⚠️ Nothing to commit"

  # =====================
  # 📂 DIFF INTELIGENTE
  # =====================
  echo "📂 Generating diff..."

  git diff $BASE_COMMIT HEAD > workspace/diff_full.txt
  git diff --name-only $BASE_COMMIT HEAD > workspace/files.txt

  # 🔥 pega só linhas relevantes (melhor custo/benefício)
  git diff $BASE_COMMIT HEAD | grep -E "^[+-]" | head -n 300 > workspace/diff.txt || true

  echo "📄 Changed files:"
  cat workspace/files.txt || true

  # valida se tem mudança
  if [ ! -s workspace/files.txt ]; then
    echo "⚠️ No relevant changes detected, skipping tester"
    continue
  fi

  # =====================
  # 🧪 TESTER (contexto reduzido)
  # =====================
  echo "🧪 Running TESTER..."

  TESTER_SYSTEM="$TESTER_SKILL

Key rules:
- Controllers are thin
- Business logic is in services
- JWT is required for protected endpoints
- Do not assume missing endpoints exist
"

  TESTER_INPUT="Analyze the following changes.

Changed files:
$(cat workspace/files.txt)

Relevant diff (partial):
$(cat workspace/diff.txt)

Focus ONLY on what changed. Validate behavior, detect risks, and update or create tests."

  TESTER_PROMPT="$TESTER_SYSTEM"$'\n\n'"$TESTER_INPUT"

  codex exec "$TESTER_PROMPT"

  # =====================
  # 🧪 VALIDAÇÃO REAL (opcional mas recomendado)
  # =====================
  echo "🧪 Running local tests..."

  if [ -f "backend/farmapp/pom.xml" ]; then
    (cd backend/farmapp && mvn -q test) || {
      echo "❌ Backend tests failed"
      exit 1
    }
  fi

  if [ -f "frontend/web/farm_web/package.json" ]; then
    (cd frontend/web/farm_web && npm run build --silent) || {
      echo "❌ Frontend build failed"
      exit 1
    }
  fi

  echo "✅ Feature completed successfully"

done

echo "===================================="
echo "🎉 ALL FEATURES COMPLETED"
echo "===================================="