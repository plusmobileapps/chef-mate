# Shared Agent Configuration

This directory is the shared source of truth for project-specific agent context.

- `seed.md` is the initial project seed used by both Claude (`CLAUDE.md`) and Codex (`AGENTS.md`).
- `skills/` contains task playbooks shared by both agents.
- `.claude/skills` and `.codex/skills` point here so skill updates stay in sync.

Keep agent-specific boot files thin. Put durable project guidance in `seed.md`, and put repeatable task workflows in `skills/`.
