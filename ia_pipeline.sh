for FILE in workspace/features/*.md; do

  echo "🚀 Processing $FILE"

  FEATURE=$(cat "$FILE")

  BASE_COMMIT=$(git rev-parse HEAD)

  DEV_SYSTEM="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/dev.md)"
  DEV_PROMPT="$DEV_SYSTEM"$'\n\n'"$FEATURE"
  echo "🚀🚀🚀 prompt $DEV_PROMPT"

  codex exec "$DEV_PROMPT"

  git add .
  git commit -m "feat(ai): $(basename "$FILE")" || true

  git diff $BASE_COMMIT HEAD > workspace/diff.txt
  git diff --name-only $BASE_COMMIT HEAD > workspace/files.txt

  TESTER_SYSTEM="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/tester.md)"

  TESTER_INPUT="Analyze this diff:

$(cat workspace/diff.txt)"

  TESTER_PROMPT="$TESTER_SYSTEM"$'\n\n'"$TESTER_INPUT"

  codex exec "$TESTER_PROMPT"

done