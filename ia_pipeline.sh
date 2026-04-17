SYSTEM_PROMPT="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/dev.md)"
INPUT_PROMPT="$(cat prompt.txt)"

echo "$INPUT_PROMPT"


codex \
  --system "$SYSTEM_PROMPT" \
  --input "$INPUT_PROMPT"