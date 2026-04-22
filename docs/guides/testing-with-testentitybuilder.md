# Guide: Testing with TestEntityBuilder

## Purpose

This guide explains how to use `TestEntityBuilder` to rapidly create test entity instances with auto-populated random values. It is written as practical instructions for developers and AI agents building tests for Ebean applications.

`TestEntityBuilder` eliminates boilerplate test setup by automatically generating realistic test data for all scalar fields, while respecting entity constraints and relationships. This is particularly valuable for:

- **Integration tests** that need representative data without caring about specific values
- **Persistence layer tests** that verify save/update/delete operations work correctly
- **Query and filter tests** where you need multiple entities with varied data
- **Rapid test setup** that reduces test code verbosity and improves readability

---

## Setup & Dependencies

### Add ebean-test to Your Project

The `TestEntityBuilder` class is provided by the `ebean-test` module.

**Maven:**
```xml
<dependency>
  <groupId>io.ebean</groupId>
  <artifactId>ebean-test</artifactId>
  <version>${ebean.version}</version>
  <scope>test</scope>
</dependency>
```

**Gradle:**
```gradle
testImplementation "io.ebean:ebean-test:${ebeanVersion}"
```

Use a version that matches your Ebean runtime (`ebean.version` /
`ebeanVersion`), or replace with an explicit fixed version if your build does
not centralize dependency versions.

> **Minimum version:** `TestEntityBuilder` was introduced in `ebean-test 17.5.0`. If your
> existing Ebean version is below this, upgrade before proceeding — mismatched Ebean
> runtime and test versions are not supported.

### Import the Class

```java
import io.ebean.test.TestEntityBuilder;
```

---

## Basic Usage

### Create a Builder Instance

`TestEntityBuilder` uses a builder pattern for configuration:

```java
TestEntityBuilder builder = TestEntityBuilder.builder(database).build();
```

The `Database` parameter specifies which Ebean database instance to use for entity type
lookups and persistence operations. Pass the injected `Database` bean (see
[Using with Dependency Injection](#using-with-dependency-injection) below) rather than
`DB.getDefault()` when working in a Spring or Avaje Inject context. For the same reason,
use the injected `database` bean for **all** persistence operations in your tests
(`database.save()`, `database.find()`, etc.) rather than mixing in static `DB.*` calls.

### Build an Entity (In-Memory)

The `build()` method creates an instance with populated fields **without persisting to the database:**

```java
Product product = builder.build(Product.class);

// Fields are populated:
// - id: unset (typically 0 for primitive long, null for boxed Long)
// - name: random UUID-based string
// - price: random BigDecimal
// - inStock: true
// - createdAt: current instant
// - etc.

// Not persisted yet (`@Id` is still unset until the entity is persisted).
```

### Build and Save (Persist to Database)

The `save()` method creates, persists, and returns an entity with the database-assigned `@Id`:

```java
Product product = builder.save(Product.class);

// Entity is now in the database:
assert database.find(Product.class, product.getId()) != null;
```

### Save Multiple Entities

The `saveAll()` method persists multiple pre-built entities in a single call:

```java
Product p1 = builder.build(Product.class);
Product p2 = builder.build(Product.class);
builder.saveAll(p1, p2);

// Both are now in the database with assigned IDs:
assert p1.getId() != null;
assert p2.getId() != null;
```

This is equivalent to `database.saveAll(p1, p2)` but avoids needing a separate
`Database` reference in tests that already hold a `TestEntityBuilder`.

### Access the Underlying Database

The `database()` method returns the `Database` instance used internally by the builder.
This is useful in tests where you want a single injected object (`TestEntityBuilder`) but
still need to perform `find()`, `delete()`, or other database operations:

```java
Product saved = builder.save(Product.class);

// Use builder.database() instead of injecting a separate Database bean:
Product found = builder.database().find(Product.class, saved.getId());
assert found != null;
```

---

## Using with Dependency Injection

Most applications using Ebean also use a DI framework. The recommended pattern is to
register `TestEntityBuilder` as a bean in the test DI context so it can be injected
directly into test classes — eliminating `@BeforeEach` setup boilerplate entirely.

### Spring Boot — `@TestConfiguration`

Add a `@TestConfiguration` class that provides `TestEntityBuilder` as a bean:

```java
@TestConfiguration
class TestConfig {

  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }
}
```

Then inject it directly into test classes:

```java
@SpringBootTest
class OrderControllerTest {

  @Autowired Database database;
  @Autowired TestEntityBuilder builder;

  @Test
  void findByStatus() {
    var order = builder.build(Order.class).setStatus(OrderStatus.PENDING);
    database.save(order);

    // ... test assertions
  }
}
```

### Avaje Inject — `@TestScope @Factory`

Add a `@Bean` method to your test-scoped `@Factory` class:

```java
@TestScope
@Factory
class TestConfiguration {

  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }
}
```

Then inject it directly into test classes using `@InjectTest`:

```java
@InjectTest
class OrderControllerTest {

  @Inject Database database;
  @Inject TestEntityBuilder builder;

  @Test
  void findByStatus() {
    var order = builder.build(Order.class).setStatus(OrderStatus.PENDING);
    database.save(order);

    // ... test assertions
  }
}
```

Both patterns produce a single shared `TestEntityBuilder` instance, wired
from the managed `Database` bean — no `@BeforeEach` required.

---

## Type-Specific Value Generation

`TestEntityBuilder` generates appropriate random values for each Java/SQL type. Customize this behavior by subclassing `RandomValueGenerator` (see "Custom Value Generators" below).

| Type | Generated Value | Notes |
|------|-----------------|-------|
| `String` | UUID-derived (8 chars by default) | Truncated to column length if `@Column(length=...)` is set |
| Email fields | `uuid@domain.com` format | Detected when property name contains "email" (case-insensitive) |
| `Integer` / `int` | Random in `[1, 1_000)` | |
| `Long` / `long` | Random in `[1, 100_000)` | |
| `Short` / `short` | Random in `[1, 100)` | See note on flag fields below |
| `Double` / `double` | Random in `[1, 100)` | |
| `Float` / `float` | Random in `[1, 100)` | |
| `BigDecimal` | Respects precision and scale | Precision and scale from `@Column(precision=..., scale=...)` |
| `Boolean` / `boolean` | `true` | Override in custom generator if needed |
| `UUID` | Random UUID | Via `UUID.randomUUID()` |
| `LocalDate` | Today's date | Via `LocalDate.now()` |
| `LocalDateTime` | Current datetime | Via `LocalDateTime.now()` |
| `Instant` | Current instant | Via `Instant.now()` |
| `OffsetDateTime` | Current time with zone | Via `OffsetDateTime.now()` |
| `ZonedDateTime` | Current time with zone | Via `ZonedDateTime.now()` |
| `Enum` | First constant | Override in custom generator if needed |
| Other types | `null` | Set these fields manually in tests |

### String Length Constraints

`TestEntityBuilder` respects column length constraints defined in the entity:

```java
@Entity
public class User {
  @Column(length = 50)
  private String username;
}

User user = builder.build(User.class);
assert user.getUsername().length() <= 50;  // ✅ Constraint respected
```

### BigDecimal Precision and Scale

For `BigDecimal` fields, the builder respects the database column precision and scale:

```java
@Entity
public class LineItem {
  @Column(precision = 10, scale = 2)  // max 99_999_999.99
  private BigDecimal amount;
}

LineItem item = builder.build(LineItem.class);
assert item.getAmount().scale() == 2;
```

### Short Fields Used as Boolean Flags

Some legacy schemas use `short` to represent boolean-like flags (e.g. `active = 1`
means active, `0` means inactive). `TestEntityBuilder` generates a random short in
`[1, 100)`, which will be non-zero but not necessarily `1`. If your application
code checks `entity.getActive() == 1` specifically, override the field after building:

```java
Organisation org = builder.build(Organisation.class)
    .setActive((short) 1);  // explicit override — random short won't do
```

---

## Entity Relationships

### Cascade-Persist Relationships: Recursively Built

Relationships marked with `cascade = PERSIST` are recursively populated:

```java
@Entity
public class Order {
  @ManyToOne(cascade = CascadeType.PERSIST)
  private Customer customer;
}

Order order = builder.build(Order.class);

// Both order and customer are built:
assert order != null;
assert order.getCustomer() != null;
// Before persist, @Id values are typically unset
// (0 for primitive IDs, null for boxed IDs).

// When saved, cascade handles both:
Order saved = builder.save(Order.class);
assert saved.getId() != null;
assert saved.getCustomer().getId() != null;  // parent also saved
```

### Non-Cascade Relationships: Left Null

Relationships without cascade persist are not auto-created — even if marked `optional = false`.
Create and save the related entity first (the builder works well here), then assign it manually
before saving the parent:

```java
@Entity
public class BlogPost {
  @ManyToOne
  private Author author;  // No cascade = left null by builder
}

BlogPost post = builder.build(BlogPost.class);
assert post.getAuthor() == null;

// Use the builder to create the related entity, then set it manually:
Author author = builder.save(Author.class);
post.setAuthor(author);
database.save(post);
```

### Collection Relationships: Left Empty

Collection relationships (`@OneToMany`, `@ManyToMany`) are left empty. On Ebean-enhanced
entities these fields are initialised to empty Ebean-managed lists (not `null`), so calling
`.add()` or `.addAll()` directly is safe:

```java
@Entity
public class Author {
  @OneToMany(mappedBy = "author")
  private List<BlogPost> posts;  // Left empty
}

Author author = builder.build(Author.class);
assert author.getPosts().isEmpty();

// Populate if needed for testing:
author.getPosts().addAll(Arrays.asList(post1, post2, post3));
```

### Cycle Detection: Prevents Infinite Recursion

If two entities reference each other with cascade persist, the builder detects the cycle and breaks it by leaving one reference null:

```java
@Entity
public class Person {
  @ManyToOne(cascade = CascadeType.PERSIST)
  private Organization org;
}

@Entity
public class Organization {
  @ManyToOne(cascade = CascadeType.PERSIST)
  private Person founder;
}

Person person = builder.build(Person.class);
// One reference will be null to break the cycle:
// either person.org or person.org.founder is null
```

---

## Custom Value Generators

### Why Customize?

The default `RandomValueGenerator` uses generic random values. For domain-specific testing, you may want:

- Email addresses with your company domain
- Realistic phone numbers
- Product SKUs following a pattern
- Addresses in specific regions
- Monetary amounts within realistic ranges

### Creating a Custom Generator

Subclass `RandomValueGenerator` and override individual `random*()` methods:

```java
class CompanyTestDataGenerator extends RandomValueGenerator {

  @Override
  protected String randomString(String propName, int maxLength) {
    if (propName != null && propName.toLowerCase().contains("email")) {
      // Use company domain instead of generic @domain.com
      String localPart = UUID.randomUUID().toString().substring(0, 8);
      String email = localPart + "@mycompany.com";
      if (maxLength > 0 && email.length() > maxLength) {
        return email.substring(0, maxLength);
      }
      return email;
    }
    return super.randomString(propName, maxLength);
  }

  // Override other methods as needed:
  @Override
  protected Object randomEnum(Class<?> type) {
    if (type == OrderStatus.class) {
      // Bias towards common statuses for realistic test data
      return ThreadLocalRandom.current().nextDouble() < 0.8
        ? OrderStatus.PENDING
        : OrderStatus.COMPLETED;
    }
    return super.randomEnum(type);
  }
}
```

### Using a Custom Generator

Pass the custom generator when building:

```java
TestEntityBuilder builder = TestEntityBuilder.builder(database)
    .valueGenerator(new CompanyTestDataGenerator())
    .build();

User user = builder.build(User.class);
assert user.getEmail().endsWith("@mycompany.com");
```

In a DI context, register this as the bean:

```java
// Spring Boot
@Bean
TestEntityBuilder testEntityBuilder(Database database) {
  return TestEntityBuilder.builder(database)
      .valueGenerator(new CompanyTestDataGenerator())
      .build();
}
```

### Example: Money Type

```java
public class MoneyValueGenerator extends RandomValueGenerator {

  @Override
  protected BigDecimal randomBigDecimal(int precision, int scale) {
    // Generate prices in a realistic range: $5.00 to $999.99
    BigDecimal price = BigDecimal.valueOf(
      ThreadLocalRandom.current().nextDouble(5.0, 1000.0)
    );
    return price.setScale(2, RoundingMode.HALF_UP);
  }
}
```

---

## Best Practices

### 1. Use for Integration Tests, Not Unit Tests

✅ **Good:** Integration test with database
```java
@Test
void whenSaving_thenCanRetrieve() {
  Product product = builder.save(Product.class);
  Product found = database.find(Product.class, product.getId());
  assertThat(found).isNotNull();
}
```

❌ **Poor:** Validation test requiring specific values
```java
@Test
void whenNameIsBlank_thenThrowException() {
  Product product = builder.build(Product.class);  // name is random!
  product.setName("");  // have to override anyway
  // ... test proceeds
}
```

### 2. Override Values for Specific Test Scenarios

When test requirements demand specific field values, manually override after building:

```java
@Test
void whenStockIsLow_thenShowWarning() {
  Product product = builder.build(Product.class);
  product.setQuantity(2);  // Specific value for this test

  boolean shouldWarn = product.shouldShowLowStockWarning();
  assertThat(shouldWarn).isTrue();
}
```

### 3. Create Fixture Factories for Common Patterns

For shared domain-specific setup, encapsulate build patterns in an instance helper class
rather than a static factory. In a DI context, this class can be registered as a bean
alongside `TestEntityBuilder`:

```java
// Spring Boot
@TestConfiguration
class TestConfig {

  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }

  @Bean
  OrderTestFactory orderTestFactory(TestEntityBuilder builder, Database database) {
    return new OrderTestFactory(builder, database);
  }
}

public class OrderTestFactory {

  private final TestEntityBuilder builder;
  private final Database database;

  public OrderTestFactory(TestEntityBuilder builder, Database database) {
    this.builder = builder;
    this.database = database;
  }

  public Order savePendingOrder() {
    Order order = builder.build(Order.class);
    order.setStatus(OrderStatus.PENDING);
    database.save(order);
    return order;
  }

  public Order saveShippedOrder() {
    Order order = builder.build(Order.class);
    order.setStatus(OrderStatus.SHIPPED);
    order.setShippedAt(Instant.now());
    database.save(order);
    return order;
  }
}

// Usage in tests:
@SpringBootTest
class OrderControllerTest {

  @Autowired OrderTestFactory orderFactory;

  @Test
  void whenOrderPending_thenCanUpdate() {
    Order order = orderFactory.savePendingOrder();
    // ... test logic
  }
}
```

### 4. Build Multiple Distinct Instances

Each call to `build()` or `save()` produces a new instance with fresh random values:

```java
@Test
void whenFetchingMultipleOrders_thenAllUnique() {
  Order order1 = builder.save(Order.class);
  Order order2 = builder.save(Order.class);
  Order order3 = builder.save(Order.class);

  assertThat(order1.getId()).isNotEqualTo(order2.getId());
  assertThat(order2.getId()).isNotEqualTo(order3.getId());
  assertThat(order1.getOrderNumber()).isNotEqualTo(order2.getOrderNumber());
}
```

---

## Complete Examples

### Example 1: Integration Test with Spring Boot

Register `TestEntityBuilder` as a `@TestConfiguration` bean, then inject it alongside
the repository under test:

```java
@TestConfiguration
class TestConfig {
  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }
}

@SpringBootTest
class OrderRepositoryTest {

  @Autowired OrderRepository orderRepository;
  @Autowired TestEntityBuilder builder;

  @Test
  void whenFindingOrdersByStatus_thenReturnsMatching() {
    Order pending1 = builder.build(Order.class);
    pending1.setStatus(OrderStatus.PENDING);

    Order pending2 = builder.build(Order.class);
    pending2.setStatus(OrderStatus.PENDING);

    Order shipped = builder.build(Order.class);
    shipped.setStatus(OrderStatus.SHIPPED);

    builder.saveAll(pending1, pending2, shipped);

    List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
    assertThat(pending).hasSize(2);
  }
}
```

### Example 2: Integration Test with Avaje Inject

```java
@TestScope
@Factory
class TestConfiguration {
  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }
}

@InjectTest
class OrderControllerTest {

  @Inject TestEntityBuilder builder;

  @Test
  void whenFindingOrdersByStatus_thenReturnsMatching() {
    Order pending1 = builder.build(Order.class);
    pending1.setStatus(OrderStatus.PENDING);

    Order pending2 = builder.build(Order.class);
    pending2.setStatus(OrderStatus.PENDING);

    Order shipped = builder.build(Order.class);
    shipped.setStatus(OrderStatus.SHIPPED);

    builder.saveAll(pending1, pending2, shipped);

    // ... test assertions
  }
}
```

### Example 3: Recursive Relationship Building

```java
@Test
void whenBuildingOrderWithCustomer_thenBothPopulated() {
  Order order = builder.build(Order.class);

  // Customer is recursively built because of @ManyToOne(cascade=PERSIST)
  assertThat(order.getCustomer()).isNotNull();
  // Before persist, @Id values are typically unset
  // (0 for primitive IDs, null for boxed IDs).
  assertThat(order.getCustomer().getName()).isNotNull();

  // Saving cascades to customer:
  Order saved = builder.save(Order.class);
  assertThat(saved.getId()).isNotNull();
  assertThat(saved.getCustomer().getId()).isNotNull();
}
```

### Example 4: Custom Generator for Domain Values

```java
// Custom generator for your domain
class ECommerceTestDataGenerator extends RandomValueGenerator {
  @Override
  protected BigDecimal randomBigDecimal(int precision, int scale) {
    // Product prices typically range $10-$500
    return BigDecimal.valueOf(
      ThreadLocalRandom.current().nextDouble(10.0, 500.0)
    ).setScale(2, RoundingMode.HALF_UP);
  }
}

@Test
void usingCustomGenerator() {
  TestEntityBuilder builder = TestEntityBuilder.builder(database)
      .valueGenerator(new ECommerceTestDataGenerator())
      .build();

  Product product = builder.build(Product.class);
  assertThat(product.getPrice())
      .isBetween(BigDecimal.TEN, BigDecimal.valueOf(500.0));
}
```

---

## Troubleshooting

### "No BeanDescriptor found for [Class] — is it an @Entity?"

**Cause:** The class you're trying to build is not registered as an Ebean entity.

**Solution:** Ensure the class is annotated with `@Entity` and registered with the Database:
```java
@Entity
@Table(name = "products")
public class Product {
  // ...
}
```

### Fields are unset even though I expected them to be populated

**Cause:** `TestEntityBuilder` does **not** populate:
- `@Id` fields (identity/primary key; left unset until persist)
- `@Version` fields (optimistic locking; left unset until persist)
- `@Transient` fields
- `@OneToMany` collections
- Non-cascade `@ManyToOne` relationships

**Solution:** Set only the fields your test scenario cares about, then persist.
`@Id` and `@Version` are usually database-managed and should typically be left
unset before save:
```java
Product product = builder.build(Product.class);
product.setName("specific-name");  // test-specific override
database.save(product);            // database assigns @Id/@Version
```

### Building recursive relationships causes StackOverflowError

**Cause:** Two or more entities mutually reference each other without cycle detection.

**Solution:** This should be handled automatically by cycle detection. If not, manually set one reference to null:
```java
Person person = builder.build(Person.class);
person.getOrganization().setFounder(null);  // Break cycle
```

### Values generated are "too random" for my test

**Cause:** Default `RandomValueGenerator` uses true random values, which aren't suitable when your test needs predictable data.

**Solution:** Create a custom generator that produces deterministic values:
```java
class DeterministicTestDataGenerator extends RandomValueGenerator {
  private int counter = 0;

  @Override
  protected String randomString(String propName, int maxLength) {
    return "test_" + (counter++);
  }
}
```

---

## Summary

`TestEntityBuilder` accelerates test development by:

1. **Reducing boilerplate** — No need to manually set every field
2. **Improving readability** — Tests focus on what matters, not setup
3. **Enabling variety** — Each build produces distinct random values
4. **Respecting constraints** — Column lengths and decimal scales are enforced
5. **Supporting customization** — Extend `RandomValueGenerator` for domain needs

