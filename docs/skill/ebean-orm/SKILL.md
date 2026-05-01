---
name: ebean-orm
description: Ebean ORM guidance for entity modeling, querying, persistence, testing, setup, and migrations. Use when working with Ebean entities, queries, database code, or project setup.
---

# Ebean ORM

Ebean is a Java/Kotlin ORM for SQL databases with compile-time enhancement, query beans, and zero-boilerplate entity modeling.

## Key Principles

- Ebean enhances entity beans at compile time — no runtime proxies
- Fields must be non-public (private, protected, or package-private)
- Add getters/setters for all fields that application code needs to read or write
- No default constructor, equals/hashCode, or toString required (all enhanced automatically)
- Prefer query beans (`QCustomer`) for type-safe queries
- Prefer primitive `long` for `@Id` and `@Version` fields
- Use `@WhenCreated` / `@WhenModified` for audit timestamps
- Use a `Database` instance (`database.save()`, `database.find()`) not static `DB.*` methods

## Task Guides

Load the relevant reference guide for the current task. **Only load what you need** — don't read all of them at once.

| Task | Reference |
|------|-----------|
| Project setup, dependencies, config | [setup](references/setup.md) |
| Entity modeling, relationships, Lombok | [modeling](references/modeling.md) |
| Queries, query beans, fetch tuning, DTOs | [querying](references/querying.md) |
| Persist, save, update, delete, transactions | [write-transactions](references/write-transactions.md) |
| Testing patterns and tools | [testing](references/testing.md) |
| DB migration generation and execution | [db-migrations](references/db-migrations.md) |

## Quick Reference

### Entity Pattern

```java
@Entity
public class Customer {
  @Id long id;
  @Version long version;
  @WhenCreated Instant whenCreated;
  @WhenModified Instant whenModified;

  String name;
  String email;

  @OneToMany(mappedBy = "customer")
  List<Order> orders;

  public long getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public List<Order> getOrders() { return orders; }
}
```

### Query Bean Pattern

```java
var customers = new QCustomer()
    .name.istartsWith("rob")
    .orders.status.eq(OrderStatus.ACTIVE)
    .setMaxRows(50)
    .findList();
```

### Save/Persist

```java
var customer = new Customer();
customer.setName("Alice");
customer.setEmail("alice@example.com");
database.save(customer);
```

### Transactions

```java
try (Transaction txn = database.beginTransaction()) {
  var order = new Order();
  order.setCustomer(customer);
  database.save(order);

  var item = new OrderItem(order, product, qty);
  database.save(item);

  txn.commit();
}
```

## Regenerating References

The reference files in `references/` are generated from the source guides in
`docs/guides/`. To regenerate after editing guides:

```bash
./docs/skill/generate-references.sh
```

Commit the updated references alongside the guide changes.
