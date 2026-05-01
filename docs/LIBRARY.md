# Ebean ORM Library Definition

Ebean is an ORM library for Java and Kotlin focused on relational data access, type-safe query construction, and production-friendly SQL behavior.

## Identity

- **Name**: Ebean ORM
- **Package**: `io.ebean`
- **Primary Maven Group**: `io.ebean`
- **Category**: ORM / Data Access
- **Repository**: https://github.com/ebean-orm/ebean
- **Issues**: https://github.com/ebean-orm/ebean/issues
- **Discussions**: https://github.com/ebean-orm/ebean/discussions
- **Website**: https://ebean.io/
- **Documentation**: https://ebean.io/docs/
- **License**: Apache 2.0

## Version & Requirements

- **Repository Version (this checkout)**: `16.5.0` (from repository `pom.xml`)
- **Minimum Java Version**: 11+
- **Languages**: Java, Kotlin
- **Build Tooling in this docs set**: Maven-focused examples

## Core Artifacts

| Artifact | Purpose |
|------|------|
| `io.ebean:ebean` | Core ORM runtime and API |
| `io.ebean:ebean-postgres` | PostgreSQL platform bundle used in setup guides |
| `io.ebean:ebean-test` | Test support, including Docker-backed database testing |
| `io.ebean:querybean-generator` | Generates `Q*` type-safe query beans |
| `io.ebean:ebean-maven-plugin` | Bytecode enhancement for entities at build time |
| `io.ebean:ebean-migration` | Runtime migration runner (often transitive via platform artifact) |

## Core APIs & Annotations

### Database and transaction APIs

| API | Purpose | Example |
|------|------|------|
| `DB.getDefault()` | Access default `Database` | `Database db = DB.getDefault();` |
| `DB.byName("...")` | Access named `Database` | `Database reporting = DB.byName("reporting");` |
| `database.find(...)` | Query entities | `Customer c = database.find(Customer.class, id);` |
| `database.insert/save/update/delete` | Persist entity changes | `database.save(customer);` |
| `database.beginTransaction()` | Manual transaction boundary | `try (Transaction txn = database.beginTransaction()) { ... }` |
| `Database.builder()` | Programmatic `Database` setup | `Database.builder().loadFromProperties().build();` |

### Query APIs

| API | Purpose | Example |
|------|------|------|
| `Q*` query beans | Type-safe query construction | `new QCustomer().status.equalTo(ACTIVE).findList();` |
| `exists()` | Efficient existence checks | `new QCustomer().email.equalTo(email).exists();` |
| `findOne()` | Unique/single-row retrieval | `new QCustomer().id.equalTo(id).findOne();` |
| `findList()` | List retrieval | `new QCustomer().findList();` |
| `asDto(...).findList()` | DTO projection reads | `new QOrder().asDto(OrderSummary.class).findList();` |

### Entity mapping and lifecycle annotations

| Annotation | Purpose |
|------|------|
| `@Entity` | Marks class as persistent entity |
| `@Id` | Primary key mapping |
| `@Version` | Optimistic locking |
| `@WhenCreated` | Creation timestamp management |
| `@WhenModified` | Modification timestamp management |
| `@Transactional` | Declarative transaction boundary |

## Capabilities

### ✅ Included

- Relational ORM with automatic dirty checking and lazy loading (via enhancement)
- Multiple query abstraction levels (ORM query, DTO query, SQL/JDBC)
- Type-safe query beans (`Q*`) with IDE autocomplete
- Built-in migration generation and migration running support
- Transaction APIs for implicit, declarative, and explicit transaction control
- Support for test-time Docker database workflows
- Query tuning and caching features for performance-sensitive workloads

### ❌ Not in scope

- HTTP routing, REST controllers, or web server runtime
- Dependency injection container functionality
- JSON serialization framework responsibilities
- Front-end/UI rendering concerns

Ebean is intentionally focused on persistence and data access. Pair it with a web framework and DI library as needed.

## Use Cases

### ✅ Strong fit

- SQL-backed business applications with rich domain models
- Services that need both ORM productivity and SQL-level control
- Projects requiring type-safe query authoring via generated query beans
- Teams that want migration generation integrated with entity model changes
- Integration test suites that need real database behavior (not only in-memory mocks)

### ⚠️ Consider alternatives if

- You need a full web framework (routing/controllers) rather than a persistence layer
- Your project does not use relational databases as a core storage model
- You want a single library to cover persistence, DI, and HTTP all at once

## Quick Start (Maven)

```xml
<properties>
  <ebean.version><!-- use latest stable from Maven Central --></ebean.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-postgres</artifactId>
    <version>${ebean.version}</version>
  </dependency>
  <dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-test</artifactId>
    <version>${ebean.version}</version>
    <scope>test</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>io.ebean</groupId>
      <artifactId>ebean-maven-plugin</artifactId>
      <version>${ebean.version}</version>
      <extensions>true</extensions>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>io.ebean</groupId>
            <artifactId>querybean-generator</artifactId>
            <version>${ebean.version}</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Minimal Example

```java
import io.ebean.DB;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class Customer {
  @Id
  private long id;
  private String name;

  public void setName(String name) {
    this.name = name;
  }
}

Database database = DB.getDefault(); // or injected

Customer customer = database.find(Customer.class, 42);
customer.setName("Updated");
database.save(customer);
```

## Common Tasks & Guides

| Task | Guide |
|------|------|
| Add Ebean to an existing Maven project | [add-ebean-postgres-maven-pom.md](guides/add-ebean-postgres-maven-pom.md) |
| Configure database and `Database` bean | [add-ebean-postgres-database-config.md](guides/add-ebean-postgres-database-config.md) |
| Add PostgreSQL test container support | [add-ebean-postgres-test-container.md](guides/add-ebean-postgres-test-container.md) |
| Generate DB migrations | [add-ebean-db-migration-generation.md](guides/add-ebean-db-migration-generation.md) |
| Model entity beans correctly | [entity-bean-creation.md](guides/entity-bean-creation.md) |
| Use Lombok safely with entities | [lombok-with-ebean-entity-beans.md](guides/lombok-with-ebean-entity-beans.md) |
| Write type-safe query bean queries | [writing-ebean-query-beans.md](guides/writing-ebean-query-beans.md) |
| Persist changes and manage transactions | [persisting-and-transactions-with-ebean.md](guides/persisting-and-transactions-with-ebean.md) |
| Build test entities quickly | [testing-with-testentitybuilder.md](guides/testing-with-testentitybuilder.md) |

**Guides index**: [guides/README.md](guides/README.md)

## Related Ecosystem Docs

- [Creating DataSource Pools](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/create-datasource-pool.md)
- [AWS Aurora Read-Write Split](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/aws-aurora-read-write-split.md)
- [Connection Validation Best Practices](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/connection-validation-best-practices.md)

## AI Agent Instructions

### For Claude, GPT, and web-based agents

Use this file as the top-level reference when answering Ebean questions.

1. Check this file first for scope and capability fit.
2. Route implementation tasks to the relevant guide in **Common Tasks & Guides**.
3. Treat Ebean as the persistence layer only; avoid implying it provides HTTP/DI features.
4. Prefer type-safe query bean examples when showing query code.
5. For setup and migration changes, follow the Maven-focused guide steps exactly.

### For IDE-based agents (Copilot, Cursor, etc.)

If `docs/LIBRARY.md` is not in context automatically:

1. Read `README.md` for docs entry points.
2. Open `docs/guides/README.md` for task-specific guides.
3. Follow linked guide files directly for concrete implementation steps.

---

## Notes for Maintainers

### When to update this file

- New release that changes requirements or key APIs
- New guide added to `docs/guides/`
- Capability/scope changes that affect "Included" or "Not in scope"
- Significant migration or setup workflow changes

### Maintenance checklist

- [ ] Keep requirements and version references accurate
- [ ] Keep Common Tasks table aligned with `docs/guides/README.md`
- [ ] Keep artifact names/snippets aligned with setup guides
- [ ] Keep AI instructions aligned with current docs structure

### Link from repository README

In `README.md`, include:

```markdown
## Documentation

- [Ebean docs](https://ebean.io/docs/)
- [Library reference](docs/LIBRARY.md)
- [Step-by-step guides](docs/guides/README.md)
```
