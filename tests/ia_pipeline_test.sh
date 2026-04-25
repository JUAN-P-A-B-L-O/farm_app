#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mkdir -p "$TMP_DIR/skills" "$TMP_DIR/workspace" "$TMP_DIR/bin"

cp "$ROOT_DIR/ia_pipeline.sh" "$TMP_DIR/"
cp "$ROOT_DIR/AI_CONTEXT.md" "$TMP_DIR/"
cp "$ROOT_DIR/skills/fixer.md" "$TMP_DIR/skills/fixer.md"

cat <<'EOF' > "$TMP_DIR/prompt.txt"
PROMPT_UNDER_TEST
EOF

cat <<'EOF' > "$TMP_DIR/bin/codex"
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$1" >> "${CODEX_CALLS_LOG:?}"
printf -- '--CALL-END--\n' >> "${CODEX_CALLS_LOG:?}"
EOF
chmod +x "$TMP_DIR/bin/codex"

calls_log="$TMP_DIR/codex_calls.log"

pushd "$TMP_DIR" >/dev/null
git init -q
git config user.email "tester@example.com"
git config user.name "Tester"
git add ia_pipeline.sh AI_CONTEXT.md skills/fixer.md prompt.txt
git commit -qm "test setup"
printf '\nDIFF_MARKER\n' >> prompt.txt

CODEX_CALLS_LOG="$calls_log" PATH="$TMP_DIR/bin:$PATH" bash ./ia_pipeline.sh >/dev/null

[[ -f workspace/diff.txt ]] || {
  echo "expected workspace/diff.txt to be created"
  exit 1
}

call_count="$(grep -c '^--CALL-END--$' "$calls_log")"
[[ "$call_count" -eq 2 ]] || {
  echo "expected 2 codex calls, got $call_count"
  exit 1
}

first_call="$(sed -n '1,/^--CALL-END--$/p' "$calls_log")"
second_call="$(sed -n '/^--CALL-END--$/,/^--CALL-END--$/p' "$calls_log" | tail -n +2)"

grep -F "PROMPT_UNDER_TEST" <<<"$first_call" >/dev/null || {
  echo "first codex call did not include the task prompt"
  exit 1
}

grep -F "Analyze this diff:" <<<"$second_call" >/dev/null || {
  echo "second codex call did not include tester diff analysis prompt"
  exit 1
}

grep -F "DIFF_MARKER" <<<"$second_call" >/dev/null || {
  echo "second codex call did not include git diff contents"
  exit 1
}

popd >/dev/null
