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
cp "$ROOT_DIR/skills/dev.md" "$TMP_DIR/skills/dev.md"
cp "$ROOT_DIR/skills/tester.md" "$TMP_DIR/skills/tester.md"
cp "$ROOT_DIR/skills/fixer.md" "$TMP_DIR/skills/fixer.md"

cat <<'EOF' > "$TMP_DIR/AI_CONTEXT.md"
AI_CONTEXT_FALLBACK_MARKER
EOF

mkdir -p "$TMP_DIR/workspace/features" "$TMP_DIR/backend/farmapp"

cat <<'EOF' > "$TMP_DIR/workspace/features/improve-buttons.md"
FEATURE_UNDER_TEST
EOF

cat <<'EOF' > "$TMP_DIR/artifact.txt"
BASELINE
EOF

cat <<'EOF' > "$TMP_DIR/bin/date"
#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "+%Y%m%d_%H%M%S" ]]; then
  printf '%s\n' "20260428_101112"
  exit 0
fi

/bin/date "$@"
EOF
chmod +x "$TMP_DIR/bin/date"

cat <<'EOF' > "$TMP_DIR/bin/codex"
#!/usr/bin/env bash
set -euo pipefail

prompt="${2:-}"
printf '%s\n' "$prompt" >> "${CODEX_CALLS_LOG:?}"
printf -- '--CALL-END--\n' >> "${CODEX_CALLS_LOG:?}"

count=0
if [[ -f "${CODEX_COUNT_FILE:?}" ]]; then
  count="$(cat "${CODEX_COUNT_FILE:?}")"
fi

if [[ "$count" -eq 0 ]]; then
  printf '\nDEV_CHANGE\n' >> artifact.txt
fi

printf '%s\n' $((count + 1)) > "${CODEX_COUNT_FILE:?}"
EOF
chmod +x "$TMP_DIR/bin/codex"

cat <<'EOF' > "$TMP_DIR/bin/mvn"
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "stub mvn success"
EOF
chmod +x "$TMP_DIR/bin/mvn"

calls_log="$TMP_DIR/codex_calls.log"
count_log="$TMP_DIR/codex_count.log"

pushd "$TMP_DIR" >/dev/null
git init -q
git config user.email "tester@example.com"
git config user.name "Tester"
git add ia_pipeline.sh AI_CONTEXT.md artifact.txt workspace/features/improve-buttons.md skills/dev.md skills/tester.md skills/fixer.md
git commit -qm "test setup"

CODEX_CALLS_LOG="$calls_log" CODEX_COUNT_FILE="$count_log" PATH="$TMP_DIR/bin:$PATH" bash ./ia_pipeline.sh >/dev/null

run_id="20260428_101112"
log_dir="workspace/logs/$run_id"
fail_dir="workspace/failures/$run_id"

[[ -d "$log_dir" ]] || {
  echo "expected timestamped log directory to be created"
  exit 1
}

[[ -d "$fail_dir" ]] || {
  echo "expected timestamped failure directory to be created"
  exit 1
}

call_count="$(grep -c '^--CALL-END--$' "$calls_log")"
[[ "$call_count" -eq 2 ]] || {
  echo "expected 2 codex calls, got $call_count"
  exit 1
}

[[ -f "$log_dir/improve-buttons_diff.txt" ]] || {
  echo "expected diff excerpt file to be created under the run log directory"
  exit 1
}

[[ -f "$log_dir/improve-buttons_files.txt" ]] || {
  echo "expected changed files list to be created under the run log directory"
  exit 1
}

grep -F -- "- Run ID: $run_id" workspace/pipeline_summary.md >/dev/null || {
  echo "pipeline summary did not record the run id"
  exit 1
}

grep -F -- "- Context file: AI_CONTEXT.md" workspace/pipeline_summary.md >/dev/null || {
  echo "pipeline summary did not record the fallback context file"
  exit 1
}

first_call="$(sed -n '1,/^--CALL-END--$/p' "$calls_log")"
second_call="$(sed -n '/^--CALL-END--$/,/^--CALL-END--$/p' "$calls_log" | tail -n +2)"

grep -F "AI_CONTEXT_FALLBACK_MARKER" <<<"$first_call" >/dev/null || {
  echo "first codex call did not include the fallback AI context contents"
  exit 1
}

grep -F "FEATURE_UNDER_TEST" <<<"$first_call" >/dev/null || {
  echo "first codex call did not include the feature contents"
  exit 1
}

grep -F "Changed files:" <<<"$second_call" >/dev/null || {
  echo "second codex call did not include the changed files section"
  exit 1
}

grep -F "artifact.txt" <<<"$second_call" >/dev/null || {
  echo "second codex call did not include the changed file list"
  exit 1
}

grep -F "DEV_CHANGE" <<<"$second_call" >/dev/null || {
  echo "second codex call did not include the git diff excerpt"
  exit 1
}

committed_files_since_setup="$(git diff --name-only HEAD~2 HEAD)"

if grep -q '^workspace/' <<<"$committed_files_since_setup"; then
  echo "workspace artifacts should not be committed by GIT_ADD_SAFE"
  exit 1
fi

popd >/dev/null
