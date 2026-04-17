SYSTEM_PROMPT="$(cat AI_CONTEXT.md)$(printf '\n\n')$(cat skills/dev.md)"
INPUT_PROMPT="$(cat prompt.txt)"

echo "$SYSTEM_PROMPT"