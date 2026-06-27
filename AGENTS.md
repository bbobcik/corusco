# Coding Agent Guidance

This repository prefers semantic MCP tools and AudEnv tools for coding-agent
work. AudEnv is the live source of truth for local development context, command
selection, and machine-specific paths.

## Tool Preference

1. Prefer semantic MCP tools for code understanding, symbol lookup, navigation,
   usage analysis, and supported refactorings. For tasks about program
   structure, first check whether suitable semantic tools are available in the
   current agent environment, then use them before plain text search or manual
   source manipulation.
2. Prefer AudEnv tools for local context, path conversion, tool availability,
   and project command recommendations.
3. Use shell tools such as `rg`, `git`, and Gradle wrappers as fallbacks or when
   AudEnv recommends them for the current project. When semantic tools are not
   available for a structure-aware task, state that fallback briefly before
   proceeding.

## AudEnv Workflow

1. Run `audenv context --project .` before local development commands.
2. Use the returned project commands, notes, paths, shell preference, tool facts,
   and recommendations as constraints for the current task.
3. Run `audenv recommend test --project .` before test commands. Prefer a
   `project-compact:` recommendation when AudEnv provides one; otherwise use the
   project command AudEnv recommends.
4. Run task-specific recommendations such as `audenv recommend build --project .`
   or `audenv recommend lint --project .` before choosing generic fallbacks for
   those tasks.
5. Convert paths with `audenv path` instead of hand-converting between Windows,
   Git Bash, WSL, POSIX, or file URI forms.
6. If AudEnv reports MCP-backed recommendations, prefer them. Otherwise use the
   shell fallbacks it recommends, such as `rg` for code search.
7. For Java code changes, prefer semantic symbol lookup, usage analysis, and
   refactoring operations over broad text-only edits when the active environment
   exposes suitable semantic MCP tools.

## Chatty Commands

- Treat Gradle and similarly verbose build tools as chatty. Do not stream full
  successful output into the conversation by default.
- Before build/test/lint tasks, ask AudEnv for the task recommendation and use
  `project-compact:` when it is listed.
- Compact invocations should store full stdout/stderr in `.machine_env/logs` or
  the log path reported by AudEnv, print a short success/failure line, preserve
  the wrapped command exit code, and show only useful failure excerpts.
- When AudEnv has no compact recommendation for a noisy one-off command, run the
  command with quiet flags where supported, capture full output to a timestamped
  temporary log under `.machine_env/logs`, and report the exit status plus the
  log path.
- Use `--quiet --stacktrace` for Gradle agent runs unless a task specifically
  requires normal lifecycle output for diagnosis.

## Migration And Legacy Files

- The project has been migrated with `audenv migrate-project --project .`.
- Review any `follow_up.*` lines from future migration output before deleting
  legacy wrappers, logs, generated artifacts, or `.machine_env` content.
- Preserve any legacy `.machine_env` detail that AudEnv did not import as
  project overlay notes before removing or rewriting legacy files.
- Do not paste large AudEnv context output into repository files. Query AudEnv
  again when local context may have changed.

## Current Project Notes

- Project commands should follow AudEnv recommendations. At migration time,
  compact output is enabled for Gradle build and test commands, with full logs
  written under `.machine_env/logs`.
- The project overlay currently records `git_bash` as the preferred shell for
  project commands.
