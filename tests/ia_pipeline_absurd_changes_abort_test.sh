#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mkdir -p "$TMP_DIR/skills" "$TMP_DIR/workspace/features" "$TMP_DIR/backend/farmapp" "$TMP_DIR/bin" "$TMP_DIR/bulk"

cp "$ROOT_DIR/ia_pipeline.sh" "$TMP_DIR/"
cp "$ROOT_DIR/skills/dev.md" "$TMP_DIR/skills/dev.md"
cp "$ROOT_DIR/skills/tester.md" "$TMP_DIR/skills/tester.md"
cp "$ROOT_DIR/skills/fixer.md" "$TMP_DIR/skills/fixer.md"

cat <<'EOF' > "$TMP_DIR/AI_CONTEXT.md"
AI_CONTEXT_FALLBACK_MARKER
EOF

cat <<'EOF' > "$TMP_DIR/workspace/features/absurd-change.md"
FEATURE_UNDER_TEST
EOF

for index in $(seq 1 26); do
  printf 'BASELINE %02d\n' "$index" > "$TMP_DIR/bulk/file_$index.txt"
done

cat <<'EOF' > "$TMP_DIR/bin/date"
#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "+%Y%m%d_%H%M%S" ]]; then
  printf '%s\n' "20260505_121500"
  exit 0
fi

/bin/date "$@"
EOF
chmod +x "$TMP_DIR/bin/date"

cat <<'EOF' > "$TMP_DIR/bin/codex"
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$2" >> "${CODEX_CALLS_LOG:?}"
printf -- '--CALL-END--\n' >> "${CODEX_CALLS_LOG:?}"

for file in bulk/*.txt; do
  printf 'DEV_CHANGE\n' >> "$file"
done
EOF
chmod +x "$TMP_DIR/bin/codex"

cat <<'EOF' > "$TMP_DIR/bin/mvn"
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "stub mvn success"
EOF
chmod +x "$TMP_DIR/bin/mvn"

calls_log="$TMP_DIR/codex_calls.log"

pushd "$TMP_DIR" >/dev/null
git init -q
git config user.email "tester@example.com"
git config user.name "Tester"
git add ia_pipeline.sh AI_CONTEXT.md workspace/features/absurd-change.md skills/dev.md skills/tester.md skills/fixer.md bulk
git commit -qm "test setup"
base_commit="$(git rev-parse HEAD)"

set +e
CODEX_CALLS_LOG="$calls_log" PATH="$TMP_DIR/bin:$PATH" bash ./ia_pipeline.sh >/dev/null 2>&1
status=$?
set -e

[[ "$status" -eq 1 ]] || {
  echo "expected pipeline to abort with exit code 1, got $status"
  exit 1
}

grep -F -- "- 🛑 Pipeline aborted: absurd changes (26 files, 26 lines)" workspace/pipeline_summary.md >/dev/null || {
  echo "pipeline summary did not record the absurd changes abort"
  exit 1
}

current_commit="$(git rev-parse HEAD)"
[[ "$current_commit" == "$base_commit" ]] || {
  echo "pipeline should abort before creating a commit"
  exit 1
}

call_count="$(grep -c '^--CALL-END--$' "$calls_log")"
[[ "$call_count" -eq 1 ]] || {
  echo "expected only the DEV agent to run before abort, got $call_count calls"
  exit 1
}

popd >/dev/null
