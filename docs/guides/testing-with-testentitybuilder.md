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

### Import the Class

```java
import io.ebean.test.TestEntityBuilder;
```

---

## Basic Usage

### Create a Builder Instance

`TestEntityBuilder` uses a builder pattern for configuration:

```java
TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault()).build();
```

The `Database` parameter (e.g., `DB.getDefault()`) specifies which Ebean database instance to use for entity type lookups and persistence operations.

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
assert DB.find(Product.class, product.getId()) != null;

// Verify it was saved:
Product found = DB.find(Product.class, product.getId());
assert found != null;
assert found.getName().equals(product.getName());
```

---

## Type-Specific Value Generation

`TestEntityBuilder` generates appropriate random values for each Java/SQL type. Customize this behavior by subclassing `RandomValueGenerator` (see "Custom Value Generators" below).

| Type | Generated Value | Notes |
|------|-----------------|-------|
| `String` | UUID-derived (8 chars by default) | Truncated to column length if `@Column(length=...)` is set |
| Email fields | `uuid@domain.com` format | Detected when property name contains "email" (case-insensitive) |
| `Integer` / `int` | Random in `[1, 1_000)` | |
| `Long` / `long` | Random in `[1, 100_000)` | |
| `Short` / `short` | Random in `[1, 100)` | |
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

Relationships without cascade persist are left null (caller can populate if needed):

```java
@Entity
public class BlogPost {
  @ManyToOne
  private Author author;  // No cascade = left null
}

BlogPost post = builder.build(BlogPost.class);
assert post.getAuthor() == null;

// Set it manually if needed:
post.setAuthor(manuallyCreatedAuthor);
```

### Collection Relationships: Left Empty

Collection relationships (`@OneToMany`, `@ManyToMany`) are left empty:

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
TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault())
    .valueGenerator(new CompanyTestDataGenerator())
    .build();

User user = builder.build(User.class);
assert user.getEmail().endsWith("@mycompany.com");
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
  Product found = DB.find(Product.class, product.getId());
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

Encapsulate common test entity setups in factory methods:

```java
public class TestDataFactory {

  private static final TestEntityBuilder builder =
    TestEntityBuilder.builder(DB.getDefault()).build();

  public static Order createPendingOrder() {
    Order order = builder.build(Order.class);
    order.setStatus(OrderStatus.PENDING);
    return order;
  }

  public static Order createShippedOrder() {
    Order order = builder.build(Order.class);
    order.setStatus(OrderStatus.SHIPPED);
    order.setShippedAt(Instant.now());
    return order;
  }

  public static Order savePendingOrder() {
    Order order = createPendingOrder();
    DB.save(order);
    return order;
  }
}

// Usage in tests:
@Test
void whenOrderPending_thenCanUpdate() {
  Order order = TestDataFactory.savePendingOrder();
  // ... test logic
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

### Example 1: Integration Test with Repository

```java
@SpringBootTest
class OrderRepositoryTest {

  @Autowired
  private OrderRepository orderRepository;

  private TestEntityBuilder builder;

  @BeforeEach
  void setup() {
    builder = TestEntityBuilder.builder(DB.getDefault()).build();
  }

  @Test
  void whenFindingOrdersByStatus_thenReturnsMatching() {
    // Create test data quickly
    Order pending1 = builder.build(Order.class);
    pending1.setStatus(OrderStatus.PENDING);

    Order pending2 = builder.build(Order.class);
    pending2.setStatus(OrderStatus.PENDING);

    Order shipped = builder.build(Order.class);
    shipped.setStatus(OrderStatus.SHIPPED);

    DB.saveAll(pending1, pending2, shipped);

    // Test the query
    List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
    assertThat(pending).hasSize(2);
  }
}
```

### Example 2: Recursive Relationship Building

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

### Example 3: Custom Generator for Domain Values

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
  TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault())
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
DB.save(product);                  // database assigns @Id/@Version
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

Use it for **integration tests and persistence layer tests** where you need representative data without specific values. For **validation tests** and **specific edge cases**, manually set the required field values.
