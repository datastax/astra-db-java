# Working Preferences — Cédric

## Coding style

- **Lombok** — use it everywhere it reduces boilerplate: `@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`, etc.
- **Clean code** — meaningful names, small focused methods, no dead code, no unnecessary comments.
- **Builder pattern** — prefer builders with method chaining for configuration and request objects; keep the API fluent and easy to read.
- **Idiomatic Java** — use Java 17+ features where appropriate (records, sealed classes, switch expressions, text blocks). Avoid patterns that feel un-Java.
- **Minimal change principle** — only change what is needed to solve the problem. Do not refactor surrounding code unless explicitly asked.

## API / library design

- Fluent, chainable APIs are preferred for the client-facing surface.
- Keep the public API surface intentional — don't expose internals.
- Sync and async variants should be consistent with each other.
- Vector search and document operations should feel natural to Java developers, not like a port of another language's style.

## Testing

- JUnit 5 + AssertJ.
- MockWebServer for HTTP-level tests.
- Tests should be readable and intention-revealing.

## Communication and collaboration style

- **Plan first, execute second.** Propose a plan before making changes; wait for approval before proceeding.
- **Validate before merging.** Present modifications for review — this is about ownership and knowing what is in the repo, not distrust.
- **Challenge and push back.** Don't just say yes — if a design is questionable, raise it.
- **Be concise.** Short, direct answers preferred. No filler text.
- Explanations should be technical and precise.

## How to work with AI (agent rules)

- Read [`AGENTS.md`](../../AGENTS.md) at the root for operational agent rules.
- Always check the wiki index before starting a task.
- After completing a task, offer to update this wiki if the work produced durable knowledge (architecture decisions, new patterns, new modules).
- Do not update the wiki without explicit owner approval.
