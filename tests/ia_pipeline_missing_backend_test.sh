#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mkdir -p "$TMP_DIR/skills" "$TMP_DIR/workspace/features" "$TMP_DIR/bin"

cp "$ROOT_DIR/ia_pipeline.sh" "$TMP_DIR/"
cp "$ROOT_DIR/skills/dev.md" "$TMP_DIR/skills/dev.md"
cp "$ROOT_DIR/skills/tester.md" "$TMP_DIR/skills/tester.md"
cp "$ROOT_DIR/skills/fixer.md" "$TMP_DIR/skills/fixer.md"

cat <<'EOF' > "$TMP_DIR/AI_CONTEXT.md"
AI_CONTEXT_FALLBACK_MARKER
EOF

cat <<'EOF' > "$TMP_DIR/workspace/features/missing-backend.md"
FEATURE_UNDER_TEST
EOF

cat <<'EOF' > "$TMP_DIR/artifact.txt"
BASELINE
EOF

cat <<'EOF' > "$TMP_DIR/bin/date"
#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "+%Y%m%d_%H%M%S" ]]; then
  printf '%s\n' "20260505_120000"
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

calls_log="$TMP_DIR/codex_calls.log"
count_log="$TMP_DIR/codex_count.log"

pushd "$TMP_DIR" >/dev/null
git init -q
git config user.email "tester@example.com"
git config user.name "Tester"
git add ia_pipeline.sh AI_CONTEXT.md artifact.txt workspace/features/missing-backend.md skills/dev.md skills/tester.md skills/fixer.md
git commit -qm "test setup"

CODEX_CALLS_LOG="$calls_log" CODEX_COUNT_FILE="$count_log" PATH="$TMP_DIR/bin:$PATH" bash ./ia_pipeline.sh >/dev/null

run_id="20260505_120000"
summary_file="workspace/pipeline_summary.md"

grep -F -- "- ⚠️ missing-backend: backend tests skipped, directory not found" "$summary_file" >/dev/null || {
  echo "pipeline summary did not record the missing backend warning"
  exit 1
}

grep -F -- "- ✅ missing-backend: completed" "$summary_file" >/dev/null || {
  echo "pipeline summary did not mark the feature as completed after skipping backend tests"
  exit 1
}

[[ ! -f "workspace/logs/$run_id/missing-backend_mvn_attempt_1.log" ]] || {
  echo "maven should not run when the backend directory is missing"
  exit 1
}

call_count="$(grep -c '^--CALL-END--$' "$calls_log")"
[[ "$call_count" -eq 2 ]] || {
  echo "expected 2 codex calls, got $call_count"
  exit 1
}

popd >/dev/null
