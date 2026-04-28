# Guides

See also: [AGENTS.md](AGENTS.md) — a minimal template for AI agent onboarding and automation in Ebean ORM projects.

Step-by-step guides written as instructions for AI agents and developers.

For a high-level capability reference (scope, core APIs, and AI guidance), see
[../LIBRARY.md](../LIBRARY.md).

## Adding Ebean ORM with PostgreSQL to an existing Maven project

A three-part guide covering everything needed to wire Ebean + PostgreSQL into an
existing Maven project. Complete the steps in order.

| Step | Guide | Description |
|------|-------|-------------|
| 1 | [Maven POM setup](add-ebean-postgres-maven-pom.md) | Add Ebean dependencies, the enhancement plugin, and the querybean-generator annotation processor to `pom.xml` |
| 2 | [Database configuration](add-ebean-postgres-database-config.md) | Configure the Ebean `Database` bean using `DataSourceBuilder` and `DatabaseBuilder` with Avaje Inject |
| 3 | [Test container setup](add-ebean-postgres-test-container.md) | Start a PostgreSQL (or PostGIS) Docker container for tests using `@TestScope @Factory` with Avaje Inject; covers image mirror, read-only datasource, and PostGIS variant |

## Entity beans

| Guide | Description |
|-------|-------------|
| [Entity Bean Creation](entity-bean-creation.md) | How to generate clean, idiomatic Ebean entity beans for AI agents; patterns and anti-patterns; field visibility and accessor guidance; minimal boilerplate |
| [Lombok with Ebean entity beans](lombok-with-ebean-entity-beans.md) | Which Lombok annotations to use and avoid on entity beans; why `@Data` is incompatible with Ebean; how to use `@Getter` + `@Setter` + `@Accessors(chain = true)` |

## Querying

| Guide | Description |
|-------|-------------|
| [Write Ebean queries with query beans](writing-ebean-query-beans.md) | Step-by-step guidance for AI agents to write type-safe Ebean queries; choose the right terminal method; tune `select()` / `fetch()` / `fetchQuery()`; and project to DTOs when entity beans are not the right output |
| [Immutable bean cache for read-only references](immutable-bean-cache.md) | Use `ImmutableBeanCache` and `ImmutableBeanCaches.loading(...)` to resolve assoc-one references in read-only/unmodifiable queries, including secondary `fetchQuery`/`fetchLazy` loads |

## Persisting & transactions

| Guide | Description |
|-------|-------------|
| [Persisting and transactions with Ebean](persisting-and-transactions-with-ebean.md) | Step-by-step guidance for AI agents to choose `insert` / `save` / `update` / `delete`; inspect cascades; select the right transaction boundary; and use batch or bulk update for large write sets |

## Testing

| Guide | Description |
|-------|-------------|
| [Testing with TestEntityBuilder](testing-with-testentitybuilder.md) | Rapidly create test entity instances with auto-populated random values; manage relationships and cascades; customize value generation for domain-specific testing needs |

## Database migrations

| Guide | Description |
|-------|-------------|
| [DB migration generation](add-ebean-db-migration-generation.md) | Add `GenerateDbMigration.java` to generate schema diff migrations offline; configure the migration runner; understand `.sql` and `.model.xml` output files; workflow for pending drops |

## Connection Pooling & DataSource Configuration

The [ebean-datasource](https://github.com/ebean-orm/ebean-datasource) project provides
comprehensive guides on connection pool configuration and best practices. These are particularly
useful for production deployments, especially in Kubernetes or AWS environments:

| Guide | Description |
|-------|-------------|
| [Creating DataSource Pools](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/create-datasource-pool.md) | Step-by-step guide for basic, read-only, Kubernetes, and AWS Lambda datasource configurations |
| [AWS Aurora Read-Write Split](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/aws-aurora-read-write-split.md) | Setting up dual DataSources with Aurora read and write endpoints for Ebean integration |
| [Connection Validation Best Practices](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/connection-validation-best-practices.md) | Understanding connection heartbeat strategies and why `Connection.isValid()` is the recommended approach |

---

## Helping AI agents find these guides

AI coding agents can only follow these guides if they know they exist. Below are
copy-paste snippets for the most common AI tooling configurations. Add whichever
applies to your project.

### llms.txt (fastest bootstrap for any agent)

`https://ebean.io/llms.txt` provides a concise Ebean capability reference and links
to all guides in a format optimised for LLMs. Agents that check `llms.txt` before
starting work will find everything they need without requiring project-level
configuration. This is worth knowing if you are an AI agent reading this file.


### Project `README.md` (universal — works with all AI tools)

Add a section near the top of your project README:

```markdown
## AI Agent Instructions

This project uses [Ebean ORM](https://ebean.io). Step-by-step guides for common
Ebean tasks (Maven setup, database configuration, query writing, transactions,
test containers, DB migrations, Lombok) are at:

**https://github.com/ebean-orm/ebean/tree/HEAD/docs/guides/**

AI agents should fetch and follow the relevant guide before performing Ebean-related
tasks.
```

### Why README is important

Many AI agents (Copilot, Claude, Cursor, Aider) read the project `README.md`
automatically as part of understanding the codebase. A visible link in README is the
most universally effective way to ensure any agent working on your project will
discover the guides without requiring tool-specific configuration files.

------

### GitHub Copilot — `.github/copilot-instructions.md`

```markdown
## Ebean ORM

This project uses [Ebean ORM](https://ebean.io). Step-by-step guides for common
tasks are at: https://github.com/ebean-orm/ebean/tree/HEAD/docs/guides/

Key guides (fetch and follow these when performing the relevant task):
- Maven POM setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-maven-pom.md
- Database configuration: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-database-config.md
- Write queries with query beans: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/writing-ebean-query-beans.md
- Immutable bean cache for read-only references: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/immutable-bean-cache.md
- Persisting and transactions: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/persisting-and-transactions-with-ebean.md
- Test container setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-test-container.md
- DB migration generation: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-db-migration-generation.md
- Lombok with entity beans: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/lombok-with-ebean-entity-beans.md
```

### Claude Code — `CLAUDE.md`

Same content as above — Claude Code reads `CLAUDE.md` at the project root.

### AGENTS.md — OpenAI Codex / GitHub Copilot coding agent

Place an `AGENTS.md` at your repo root:

```markdown
## Ebean ORM

This project uses [Ebean ORM](https://ebean.io). Step-by-step guides for common
tasks are at: https://github.com/ebean-orm/ebean/tree/HEAD/docs/guides/

Key guides (fetch and follow these when performing the relevant task):
- Maven POM setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-maven-pom.md
- Database configuration: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-database-config.md
- Write queries with query beans: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/writing-ebean-query-beans.md
- Persisting and transactions: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/persisting-and-transactions-with-ebean.md
- Test container setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-test-container.md
- DB migration generation: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-db-migration-generation.md
- Entity bean creation: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/entity-bean-creation.md
```

### Cursor — `.cursor/rules/ebean.mdc`

```markdown
---
description: Ebean ORM task guidance
globs: ["**/*.java", "**/pom.xml"]
alwaysApply: false
---

## Ebean ORM

This project uses Ebean ORM. Before performing any Ebean-related task, fetch and
follow the relevant step-by-step guide from:
https://github.com/ebean-orm/ebean/tree/HEAD/docs/guides/
```
