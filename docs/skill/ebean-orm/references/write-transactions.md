# Ebean ORM Bundle — Write & Transactions (Flattened)

> Flattened bundle. Content from source markdown guides is inlined below.

---

## Source: `persisting-and-transactions-with-ebean.md`

# Guide: Persist Changes and Manage Transactions with Ebean

## Purpose

This guide gives step-by-step instructions for AI agents and developers to save,
update, delete, and batch changes with Ebean while choosing the correct
transaction boundary.

Use this guide when you need to:

- create a new entity row
- update one or more existing rows
- delete rows safely
- decide between implicit transactions, `@Transactional`, and explicit
  transactions
- batch or bulk-write many rows efficiently

The default recommendation is:

1. Choose the correct persistence operation first
2. Use implicit transactions for a single isolated write
3. Use `@Transactional` for multi-step application workflows
4. Use explicit transactions only when you need explicit control
5. Use bulk update or batching for large write sets

---

## Prerequisites

- The project already uses Ebean ORM
- Entity beans and database configuration already exist
- You know which `Database` is being used (`DB.getDefault()` or a named database)

If the project is not yet configured, first follow:

- [`add-ebean-postgres-database-config.md`](add-ebean-postgres-database-config.md)
- [`entity-bean-creation.md`](entity-bean-creation.md)

---

## Step 1 - Choose the correct persistence operation before editing code

Do not start with `database.save(...)` by habit. First decide what kind of change the
caller is making.

| Need | Preferred API | Use when |
|------|---------------|----------|
| Insert a bean that is definitely new | `database.insert(bean)` | New-create flow, seed data, fixture setup |
| Save a bean that may be new or existing | `database.save(bean)` | Common default when bean state determines insert vs update |
| Update a bean that is definitely existing | `database.update(bean)` | Existing row should be updated only |
| Delete one bean | `database.delete(bean)` | Remove a loaded entity bean |
| Update many rows without loading beans | `database.update(...)` or `query.asUpdate()` | Set-based write, not per-row business logic |
| Delete many rows without loading beans | bulk update/delete API or `database.sqlUpdate(...)` | Set-based deletion |

### Agent rule

Choose the operation that matches intent:

- known new row -> `insert`
- known existing row -> `update`
- uncertain/new-or-existing -> `save`
- many rows -> bulk update/delete, not a loop of individual saves

### Style note

Use a `Database` instance for all persistence operations: `database.save(bean)`,
`database.insert(bean)`, `database.update(bean)`, `database.delete(bean)`.
Inject the `Database` bean or obtain it via `DB.getDefault()`. Avoid using the
static `DB.*` convenience methods.

---

## Step 2 - Persist single-bean changes with the correct API

### Example - insert a known new bean

```java
Customer customer = new Customer();
customer.setName("Rob");
customer.setEmail("rob@example.com");

database.insert(customer);
```

### Example - update an existing bean

```java
Customer customer = new QCustomer()
  .id.equalTo(customerId)
  .findOne();

customer.setStatus(Customer.Status.ACTIVE);

database.update(customer);
```

### When to prefer `insert()` over `save()`

Use `insert()` when the code is creating a brand new row and should fail if the
operation does not behave like an insert.

### When to prefer `update()` over `save()`

Use `update()` when the bean is definitely existing and the method should not
silently behave like an insert.

---

## Step 3 - Check cascade mappings before assuming related beans will persist or delete

Ebean follows cascade rules defined on mapping annotations such as
`@OneToMany`, `@OneToOne`, `@ManyToOne`, and `@ManyToMany`.

The default is **no cascade**.

### Example

```java
@Entity
public class Order {

  @ManyToOne
  private Customer customer; // no cascade by default

  @OneToMany(cascade = CascadeType.ALL)
  private List<OrderDetail> details; // save + delete cascade
}
```

```java
database.save(order);
```

With the mapping above:

- `details` are cascaded
- `customer` is **not** cascaded

### Agent rules for cascades

1. Inspect the mapping before writing save/delete logic
2. Do not assume `@ManyToOne` cascades
3. Avoid adding cascade to shared parent references unless ownership is truly
   intended
4. If a relationship should not cascade, save/delete related beans explicitly

---

## Step 4 - Let Ebean use an implicit transaction for a single isolated write

If the method performs one isolated persistence operation, Ebean can manage the
transaction implicitly.

### Good fit for implicit transaction

```java
Customer customer = new QCustomer()
  .id.equalTo(customerId)
  .findOne();

customer.setStatus(Customer.Status.INACTIVE);
database.save(customer);
```

### Good fit

- one save
- one update
- one delete
- small helper method with a single write

### Poor fit

- multiple writes that must commit or roll back together
- query + save + save workflow
- any method where later failure must roll back earlier writes

### Important

Queries also use implicit transactions when needed. You generally do **not**
need to wrap ordinary read queries in an explicit transaction "just in case".

---

## Step 5 - Use `@Transactional` for multi-step service workflows

When multiple Ebean operations belong to one unit of work, use
`@Transactional`.

### Example - service method

```java
import io.ebean.annotation.Transactional;

@Transactional
public void shipOrder(long orderId) {

  Order order = new QOrder()
    .id.equalTo(orderId)
    .findOne();

  order.setStatus(Order.Status.SHIPPED);
  database.save(order);

  Shipment shipment = new Shipment(order, Instant.now());
  database.insert(shipment);
}
```

All database work inside the method runs in one transaction and commits only if
the method completes successfully.

### Use `Transaction.current()` only when needed

If the method needs access to the current transaction itself:

```java
Transaction txn = Transaction.current();
```

Do this only for transaction-specific behavior such as comments, savepoints, or
other advanced control. Do not fetch the current transaction if the method does
not need it.

### Agent rules for `@Transactional`

1. Put it on application/service workflow methods, not everywhere by default
2. Keep the transaction focused on database work
3. Avoid remote HTTP calls, message publishing, or long-running CPU work inside
   the transaction if those can be moved outside

### Named database note

If the method uses a non-default database, obtain that `Database` instance via
`DB.byName("...")` and consistently use that database for both queries and
writes.

---

## Step 6 - Use `beginTransaction()` when you need explicit control

Use an explicit transaction when you need manual `commit()`, batching, explicit
flush, savepoints, or other low-level transaction control.

### Example - explicit transaction with try-with-resources

```java
try (Transaction txn = database.beginTransaction()) {

  Order order = new QOrder()
    .id.equalTo(orderId)
    .findOne();

  order.cancel();
  database.save(order);

  AuditLog auditLog = new AuditLog("order-cancelled", orderId);
  database.insert(auditLog);

  txn.commit();
}
```

If `commit()` is not reached, closing the transaction rolls it back.

### Useful explicit controls

- `txn.commit()` - commit current work
- `txn.setRollbackOnly()` - force rollback-only behavior
- `txn.flush()` - push batched statements to the database now

### Agent rule

Prefer `@Transactional` unless explicit transaction control is actually needed.
Do not use `beginTransaction()` only because it feels "safer".

---

## Step 7 - Use `createTransaction()` only for non-thread-local transaction handling

`createTransaction()` creates a transaction that is **not** placed into the
thread-local scope. This is a specialized tool.

Use it when:

- the transaction will be passed explicitly
- you need more than one transaction in the same thread
- you are coordinating work across threads or lower-level APIs

### Example - explicit transaction passed to query and save

```java
Database database = DB.getDefault();

try (Transaction txn = database.createTransaction()) {

  Customer customer = new QCustomer(txn)
    .email.equalTo(email)
    .findOne();

  customer.setInactive(true);
  database.save(customer, txn);

  txn.commit();
}
```

### Agent rule

If you are not deliberately bypassing thread-local transaction scope, do **not**
use `createTransaction()`. Most service code should use `@Transactional` or
`beginTransaction()`.

---

## Step 8 - Use bulk update/delete or JDBC batch for many-row writes

Loops of `database.save(...)` are often the wrong tool for large write sets.

### Prefer bulk update for set-based changes

If the update can be expressed as "change all rows matching this predicate",
perform one bulk update instead of loading and saving each bean.

### Example - bulk update with query beans

```java
var cust = QCustomer.alias();

int rows = new QCustomer()
  .status.equalTo(Customer.Status.NEW)
  .asUpdate()
  .set(cust.status, Customer.Status.ACTIVE)
  .update();
```

### Example - bulk update with `database.update(...)`

```java
int rows = database.update(Customer.class)
  .set("status", Customer.Status.ACTIVE)
  .where()
  .eq("status", Customer.Status.NEW)
  .update();
```

### Prefer JDBC batch for many individual inserts/updates

If each row has different values and must still go through per-bean persistence,
use batching.

```java
Database database = DB.getDefault();

try (Transaction txn = database.beginTransaction()) {
  txn.setBatchMode(true);
  txn.setBatchSize(100);
  txn.setGetGeneratedKeys(false);

  for (Customer customer : customersToInsert) {
    database.insert(customer, txn);
  }

  txn.commit();
}
```

### Alternative - annotation-driven batching

```java
@Transactional(batchSize = 50)
public void importCustomers(List<Customer> customers) {
  for (Customer customer : customers) {
    database.insert(customer);
  }
}
```

### Batch caveats

- Executing a query inside a batched transaction can flush the batch
- Mixing bean persistence and `SqlUpdate` can also flush the batch
- Accessing generated/unloaded properties on batched beans can flush the batch

If the workflow depends on delayed flushing, review the batch-flush rules before
adding more queries inside the same transaction.

---

## Common anti-patterns

### Anti-pattern 1 - Saving many rows one by one without batch or bulk update

If you are changing hundreds or thousands of rows, first ask whether it should
be a bulk update or a batched transaction.

### Anti-pattern 2 - Assuming child beans cascade automatically

Cascade is not automatic. Inspect the mapping first.

### Anti-pattern 3 - Wrapping external calls inside the database transaction

Do not keep transactions open while waiting on HTTP calls, queues, or other
slow external systems unless the design genuinely requires it.

### Anti-pattern 4 - Using `createTransaction()` for ordinary service code

Most service code should not bypass thread-local transaction handling.

### Anti-pattern 5 - Using `save()` when you really need `insert()` or `update()`

If operation intent matters, choose the more specific API.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Child beans were not saved or deleted | Missing cascade mapping | Inspect annotations and add explicit save/delete or the correct cascade |
| Earlier writes committed even though later work failed | The whole workflow was not inside one transaction | Wrap the unit of work in `@Transactional` or an explicit transaction |
| `OptimisticLockException` on update/delete | Concurrent modification or stale version | Re-fetch, merge, or handle concurrency explicitly |
| Batch writes flush earlier than expected | Query, mixed SQL, or property access triggered flush | Review batch flush rules and transaction flow |
| Explicit transaction example does not affect the expected database | Mixed default DB and named DB usage | Use the same `Database` instance consistently for query and write |

---

## Summary workflow for AI agents

When asked to add persistence logic:

1. Choose `insert`, `save`, `update`, `delete`, or bulk update based on intent
2. Inspect cascade mappings before assuming related beans will persist/delete
3. Use implicit transactions for one isolated write
4. Use `@Transactional` for multi-step units of work
5. Use `beginTransaction()` only when explicit transaction control is needed
6. Use `createTransaction()` only for explicit, non-thread-local handling
7. Use bulk update or batching for large write sets

---

## Related documentation

- [Entity Bean Creation](entity-bean-creation.md)
- [Testing with TestEntityBuilder](testing-with-testentitybuilder.md)
- [Ebean persist docs](https://ebean.io/docs/persist)
- [Ebean transaction docs](https://ebean.io/docs/transactions)
