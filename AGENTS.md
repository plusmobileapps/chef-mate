# AGENTS.md

This project shares its agent instructions between Codex and Claude.

Before working in this repository, read and follow:

- `.agents/seed.md` - shared project context, architecture, commands, testing, and git workflow.
- `.agents/skills/` - shared task playbooks. Use a skill when the user names it or the task clearly matches it.

The shared seed overrides any older or task-specific instruction that suggests pushing, opening a PR, or using a destructive command without an explicit current-message request from the user.
