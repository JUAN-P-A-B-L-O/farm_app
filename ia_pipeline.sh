# SYSTEM_PROMPT="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/dev.md)"
SYSTEM_PROMPT="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/fixer.md)"


INPUT_PROMPT="$(cat prompt.txt)"

echo "$INPUT_PROMPT"

FULL_PROMPT="$SYSTEM_PROMPT"$'\n\n'"$INPUT_PROMPT"

codex "$FULL_PROMPT"