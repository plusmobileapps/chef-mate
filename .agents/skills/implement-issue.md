# implement-issue

Take a GitHub issue, implement the fix/feature, and prepare the work for review. Only push or open a pull request when the user explicitly asks for that in the current message.

## Required Input

The user must provide a GitHub issue number or URL. If not provided, ask before proceeding.

## Instructions

### Step 1: Fetch the issue

Use `gh issue view <number>` to read the issue title, body, and labels. Understand what needs to be done before writing any code.

### Step 2: Prepare the branch

```sh
git checkout main
git pull origin main
```

Create a new branch. The branch name format is: `<issue-number>-<brief-description-with-dashes>`. Use lowercase. Keep the description to 3-5 words derived from the issue title.

Example: for issue #52 "Add dark mode support" → `52-add-dark-mode-support`

```sh
git checkout -b <branch-name>
```

### Step 3: Implement the change

Read the issue carefully and implement the fix or feature following the project's architecture and conventions described in CLAUDE.md. Run `./gradlew ktfmtFormat` after making changes.

If tests are relevant, run them with `./gradlew test` (or the appropriate module-specific test command) and ensure they pass before proceeding.

### Step 4: Commit

Stage and commit the changes. Write a clear, concise commit message that references the issue. Use conventional commit style (e.g., `feat:`, `fix:`, `refactor:`).

### Step 5: Stop unless the user explicitly asked to push

Do **not** push by default. The project workflow requires a separate, explicit user request before running `git push`.

### Step 6: Create the pull request only if requested

Only if the user explicitly asks for a PR in the current message, use the `gh` CLI to create a PR against `main`. The PR title should be concise and the body should summarize the changes and reference the issue with `Closes #<number>`.

```sh
gh pr create --title "<type>: <short description>" --body "$(cat <<'EOF'
## Summary
<bullet points summarizing the changes>

Closes #<issue-number>

## Test plan
<how to verify the changes>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

Return the PR URL to the user when done.
