# Ebean ORM Bundle — Modeling (Flattened)

> Flattened bundle. Content from source markdown guides is inlined below.

---

## Source: `entity-bean-creation.md`

# Entity Bean Creation Guide for AI Agents

**Target Audience:** AI systems (Claude, Copilot, ChatGPT, etc.)
**Purpose:** Learn how to generate clean, idiomatic Ebean entity beans
**Key Insight:** Ebean entity fields must be non-public (no public fields). Accessors don't need JavaBeans naming conventions; no manual equals/hashCode implementation is needed
**Language:** Java
**Framework:** Ebean ORM

---

## Quick Rules

Before writing entity code, remember:

| Requirement | Needed? | Notes                                                                                                                 |
|-------------|---------|-----------------------------------------------------------------------------------------------------------------------|
| `@Entity` annotation | ✅ **YES** | Marks class as persistent entity                                                                                      |
| `@Id` annotation | ✅ **YES** | Marks primary key field                                                                                               |
| Getters/setters (or other accessors) | ✅ **YES** | Needed for application code to access fields. Naming can be JavaBeans, fluent, or custom — no specific convention required. |
| Default constructor | ❌ **NO** | Not required. Ebean can instantiate without it.                                                                       |
| equals/hashCode | ❌ **NO** | Ebean auto-enhances these at compile time.                                                                            |
| toString() | ❌ **NO** | Ebean auto-enhances this. Don't implement with getters.                                                               |
| `@Version` | ⚠️ **OPTIONAL** | Use for optimistic locking. Highly recommended.                                                                       |
| `@WhenCreated` | ⚠️ **OPTIONAL** | Auto-timestamp creation time. Highly recommended. Use for audit trail.                                                |
| `@WhenModified` | ⚠️ **OPTIONAL** | Auto-timestamp modification time. Highly recommended. Use for audit trail.                                            |

**Critical:**
- Prefer primitive `long` for `@Id` and `@Version`, NOT `Long` object.
- Fields should be non-public: **private**, **protected**, or package-private.
- If you add accessors, they do NOT need to follow Java bean conventions.

---

## Naming Conventions: The D* (Domain) Prefix Pattern

Entity beans represent internal domain/persistence model details. It's a common best practice in Ebean projects to use the **D* prefix** (D for Domain) for entity class names.

**Why use D* prefix?**

1. **Avoid name clashes with DTOs** - Your public API may have `Customer` (DTO), but your entity is `DCustomer` (Domain). They're clearly different.
2. **Signal intent clearly** - The D prefix immediately tells developers "this is an internal domain class, not part of the public API"
3. **Clarify conversions** - When converting `DCustomer` → `Customer` (DTO), the direction is obvious
4. **Separate concerns** - API classes in one package (no prefix), domain classes in another (with D prefix)

**Example naming pattern:**
- Entity: `DCustomer`, `DOrder`, `DProduct`, `DInvoice`
- DTO: `Customer`, `Order`, `Product`, `Invoice`
- Converter: `DCustomerMapper.toDTO(DCustomer)` → `Customer`

**Where to place entities:**
- Entities: `com.example.domain.entity` (or `persistence`)
- DTOs: `com.example.api.model` or `com.example.dto`

**When to use D* prefix:**
- ✅ **DO** use for entity beans (internal domain model)
- ✅ **DO** use when you have parallel DTO classes with similar names
- ❌ **DON'T** use for DTOs or public API classes
- ❌ **DON'T** use if you have no DTOs and entities are your public API

Example with and without prefix:

```java
// With D* prefix (recommended - allows both entity and DTO to exist)
@Entity
public class DCustomer {
  @Id private long id;
  private String name;
  // ... entity-specific fields and methods
}

// Public API DTO (no D prefix)
public record Customer(long id, String name) {
  // ... conversion method
}

// Conversion
public static Customer toDTO(DCustomer entity) {
  return new Customer(entity.getId(), entity.getName());
}
```

This naming convention is optional but highly recommended for projects with separate domain and API layers.

---

## Minimal Entity (No Boilerplate)

This is a complete, valid Ebean entity:

```java
@Entity
public class Customer {
  @Id
  private long id;
  private String name;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
```

**Why this works:**
- ✅ `@Entity` marks it as persistent
- ✅ `@Id private long id` is the primary key
- ✅ Private fields (Ebean does NOT support public fields without expert flags enabled)
- ✅ Accessors can follow any naming convention, and can be omitted when field access is preferred
- ✅ No default constructor needed
- ✅ No equals/hashCode needed (Ebean enhances these)

**What Ebean does at compile time:**
- Enhances equals/hashCode based on @Id
- Adds field change tracking
- Enables lazy loading
- Enhances toString()

**Result:** Your entity is now fully functional with zero boilerplate.

---

## Pattern 1: Basic Entity

**Use this when:** You need a simple persistent object.

```java
@Entity
public class Product {
  @Id
  private long id;
  private String name;
  private String description;
  private BigDecimal price;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }
}
```

**What you get:**
- Primary key: `id` (private field, accessed via getter)
- Three properties: `name`, `description`, `price` (private fields, accessed via getters/setters)
- Automatic equals/hashCode based on id
- Full ORM functionality

---

## Pattern 2: Entity with Audit Trail

**Use this when:** You need to track who/when created/modified data.

```java
@Entity
public class Order {
  @Id
  private long id;
  @Version
  private long version;
  @WhenCreated
  private Instant createdAt;
  @WhenModified
  private Instant modifiedAt;

  private String orderNumber;
  private BigDecimal totalAmount;

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getModifiedAt() {
    return modifiedAt;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }
}
```

**What you get:**
- `version`: Optimistic locking (prevents concurrent update conflicts)
- `createdAt`: Automatically set when inserted (Ebean manages this)
- `modifiedAt`: Automatically updated on every modification (Ebean manages this)

**Example usage:**
```java
// Create
Order order = new Order();
order.setOrderNumber("ORD-001");
order.setTotalAmount(new BigDecimal("99.99"));
database.save(order);  // createdAt is automatically set by Ebean

// Modify
order.setTotalAmount(new BigDecimal("109.99"));
database.update(order);  // version incremented, modifiedAt updated automatically

// Check when modified
System.out.println(order.getModifiedAt());  // Current timestamp
```

---

## Pattern 3: Entity with Constructor

**Use this when:** Domain logic requires initialization or validation.

```java
@Entity
public class Invoice {
  @Id
  private long id;
  @Version
  private long version;

  private String invoiceNumber;
  private String customerName;
  private BigDecimal amount;

  public Invoice(String invoiceNumber, String customerName, BigDecimal amount) {
    this.invoiceNumber = invoiceNumber;
    this.customerName = customerName;
    this.amount = amount;
  }

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public String getCustomerName() {
    return customerName;
  }

  public BigDecimal getAmount() {
    return amount;
  }
}
```

**When to add a constructor:**
- ✅ Required fields must be set during creation
- ✅ Validation needs to happen on initialization
- ✅ Domain logic needs setup

**When NOT to add:**
- ❌ If users will just set fields afterwards anyway
- ❌ If there are many optional fields

---

## Pattern 4: Entity with Relationships

**Use this when:** You need associations to other entities.

```java
@Entity
public class Customer {
  @Id
  private long id;
  @Version
  private long version;
  @WhenCreated
  private Instant createdAt;

  private String name;
  private String email;

  @OneToMany(mappedBy = "customer")
  private List<Order> orders;  // Use List, not Set

  @ManyToOne
  private Address billingAddress;

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public Address getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
  }
}
```

**Important:**
- Use `List<>` not `Set<>` for collections (Set calls equals/hashCode before beans have IDs)
- `mappedBy` means Order.customer is the owner
- Relationships are lazy-loaded by default

---

## What NOT to Do (Anti-Patterns)

### ❌ Anti-Pattern 1: Public Fields

**DON'T:**
```java
@Entity
public class Customer {
  @Id public long id;  // ❌ Public field - not supported
  public String name;  // ❌ Public field - not supported
}
```

**DO:**
```java
@Entity
public class Customer {
  @Id
  private long id;  // ✅ Private field with getter
  private String name;  // ✅ Private field with accessors

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
```

**Why:** Ebean does NOT support public fields. Fields must be private and accessed via getters/setters or other accessor methods. Public fields bypass Ebean's tracking mechanisms and will cause data consistency issues.

---

### ❌ Anti-Pattern 2: Use Long Object Instead of Primitive

**DON'T:**
```java
@Entity
public class Customer {
  @Id
  private Long id;  // ❌ Object type
  private String name;
}
```

**DO:**
```java
@Entity
public class Customer {
  @Id
  private long id;  // ✅ Primitive type
  private String name;
}
```

**Why:** Performance, nullability semantics, Ebean optimization.

---

### ❌ Anti-Pattern 3: Implement equals/hashCode

**DON'T:**
```java
@Entity
public class Customer {
  @Id
  private long id;
  private String name;

  @Override
  public boolean equals(Object o) {  // ❌ Unnecessary
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Customer customer = (Customer) o;
    return id == customer.id;
  }

  @Override
  public int hashCode() {  // ❌ Unnecessary
    return Objects.hash(id);
  }
}
```

**DO:**
```java
@Entity
public class Customer {
  @Id
  private long id;
  private String name;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  // Ebean enhances equals/hashCode automatically
}
```

**Why:** Ebean's enhancement is optimized for ORM operations. Your implementation might conflict with Ebean's tracking.

---

### ❌ Anti-Pattern 4: Use Set for Collections

**DON'T:**
```java
@Entity
public class Customer {
  @Id
  private long id;

  @OneToMany(mappedBy = "customer")
  private Set<Order> orders;  // ❌ Set calls equals/hashCode before IDs assigned
}
```

**DO:**
```java
@Entity
public class Customer {
  @Id
  private long id;

  @OneToMany(mappedBy = "customer")
  private List<Order> orders;  // ✅ List doesn't require equals/hashCode on unsaved beans
}
```

**Why:** Set calls equals/hashCode immediately. New beans don't have IDs yet, causing issues.

---

### ❌ Anti-Pattern 5: toString() with Getters

**DON'T:**
```java
@Entity
public class Customer {
  @Id
  private long id;
  private String name;

  @Override
  public String toString() {  // ❌ Uses getters
    return "Customer{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }

  public long getId() { return id; }
  public String getName() { return name; }
}
```

**Why:** In a debugger, toString() is called automatically. Getters can trigger lazy loading, changing debug behavior.

**DO:** Either don't implement toString(), or access fields directly:
```java
@Override
public String toString() {
  return "Customer{" +
      "id=" + id +
      ", name='" + name + '\'' +
      '}';
}
```

---

### ❌ Anti-Pattern 6: @Column(name=...) for Naming Convention

**DON'T:**
```java
@Entity
public class Customer {
  @Id
  private long id;

  @Column(name = "first_name")  // ❌ Unnecessary
  private String firstName;
}
```

**DO:**
```java
@Entity
public class Customer {
  @Id
  private long id;

  private String firstName;  // ✅ Ebean uses naming convention: first_name
}
```

**Why:** Ebean's naming convention handles this automatically. Only use @Column when your database column doesn't match the convention.

---

## What Ebean Enhancement Provides

At compile time, Ebean enhances your entity classes:

1. **equals/hashCode** - Based on @Id, optimal for ORM
2. **Field change tracking** - Knows which fields were modified
3. **Lazy loading** - Collections and relationships load on demand
4. **Persistence context** - Manages identity and state
5. **toString()** - Auto-implemented (don't override with getters)

**Result:** Your entity bean is minimal, but fully featured.

---

## Field Types

**Recommended for ID/Version:**
- `long` (primitive) ✅ Use this
- `int` (primitive) ✅ Use this
- `UUID` ✅ Use this

**Not recommended:**
- `Long` object ⚠️ Avoid (use primitive long)
- `Integer` object ⚠️ Avoid (use primitive int)

**For other fields:**
- Use standard Java types: `String`, `BigDecimal`, `Instant`, `LocalDate`, etc.
- Use primitives where nullable semantics don't apply: `int`, `long`, `boolean`
- Use objects where null has meaning: `String`, `BigDecimal`, `LocalDate`

---

## Example: Building an Entity Step by Step

Start minimal, add what you need:

**Step 1: Minimal**
```java
@Entity
public class BlogPost {
  @Id long id;
  String title;
  String content;

  public long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
```

**Step 2: Add audit trail**
```java
@Entity
public class BlogPost {
  @Id long id;
  @Version long version;
  @WhenCreated Instant createdAt;
  @WhenModified Instant modifiedAt;

  String title;
  String content;

  public long getId() { return id; }
  public long getVersion() { return version; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getModifiedAt() { return modifiedAt; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
```

**Step 3: Add author relationship**
```java
@Entity
public class BlogPost {
  @Id long id;
  @Version long version;
  @WhenCreated Instant createdAt;
  @WhenModified Instant modifiedAt;

  String title;
  String content;

  @ManyToOne
  Author author;

  public long getId() { return id; }
  public long getVersion() { return version; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getModifiedAt() { return modifiedAt; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public Author getAuthor() { return author; }
  public void setAuthor(Author author) { this.author = author; }
}
```

**Step 4: Add constructor if needed**
```java
@Entity
public class BlogPost {
  @Id long id;
  @Version long version;
  @WhenCreated Instant createdAt;
  @WhenModified Instant modifiedAt;

  String title;
  String content;

  @ManyToOne
  Author author;

  public BlogPost(String title, String content, Author author) {
    this.title = title;
    this.content = content;
    this.author = author;
  }

  public long getId() { return id; }
  public long getVersion() { return version; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getModifiedAt() { return modifiedAt; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public Author getAuthor() { return author; }
  public void setAuthor(Author author) { this.author = author; }
}
```

Each step adds only what's necessary for the entity structure. Accessors are always included so application code can use the entity.

---

## Usage Examples

### Creating and saving
```java
Customer customer = new Customer();
customer.setName("Alice");
database.save(customer);  // id auto-generated
```

### Finding
```java
Customer found = database.find(Customer.class, 1);
System.out.println(found.getName());
```

### Updating
```java
found.setName("Bob");
database.update(found);  // version auto-incremented
```

### Collections (relationships)
```java
Customer customer = database.find(Customer.class, 1);
List<Order> orders = customer.getOrders();  // Lazy loads automatically
```

---

## Summary for AI Agents

When generating Ebean entity beans:

✅ **DO:**
- Use primitive `long` for @Id and @Version
- Keep entities minimal (just fields + @Entity + @Id)
- Use @Version for concurrency control
- Use @WhenCreated/@WhenModified for audit trail
- Use List for collections, not Set
- Add constructors only if domain logic requires it
- Add getters/setters for all fields that application code needs to read or write

❌ **DON'T:**
- Use Long object for @Id/@Version
- Implement equals/hashCode
- Implement toString() with getters
- Use Set for @OneToMany/@ManyToMany
- Add unnecessary @Column annotations
- Add default constructors "just in case"

**Result:** Clean, readable, maintainable entity beans with full ORM functionality and zero boilerplate.

---

## Related Documentation

- Entity Bean Best Practices: `/docs/best-practice/`
- JPA Mapping Reference: `/docs/mapping/jpa/`
- Ebean Extensions: `/docs/mapping/extensions/`
- First Entity Guide: `/docs/intro/first-entity/`

---

## Source: `lombok-with-ebean-entity-beans.md`

# Guide: Using Lombok with Ebean Entity Beans

## Purpose

This guide explains which Lombok annotations are safe and recommended for Ebean
entity beans, which ones to avoid, and why. It is written as prescriptive instructions
for AI agents and developers.

---

## The Core Rule

> **Do NOT use `@Data` on Ebean entity beans.**

Use `@Getter` + `@Setter` instead, with the optional `@Accessors(chain = true)` for
a fluent setter style.

---

## Why `@Data` is Incompatible with Ebean

`@Data` is a convenience annotation that is equivalent to applying `@Getter`,
`@Setter`, `@RequiredArgsConstructor`, `@ToString`, and `@EqualsAndHashCode` together.
Three of those are problematic for Ebean entity beans:

### 1. `@EqualsAndHashCode` (included in `@Data`) — breaks entity identity

`@Data` generates `hashCode()` and `equals()` based on all non-static, non-transient
fields. Ebean entity beans have identity semantics — two references to the same database
row should be considered equal based on their `@Id` value, not field-by-field comparison.

Problems caused:
- Inconsistent `hashCode` before and after persist (the `@Id` field is `0` on a new
  entity, then changes after insert — violating the `hashCode` contract for collections)
- Entities placed in a `Set` or `HashMap` before saving will be unfindable after saving
- Ebean's internal identity map and dirty checking can be confused

### 2. `@ToString` (included in `@Data`) — triggers unexpected lazy loading

`@Data` generates a `toString()` that accesses **all** fields, including
`@OneToMany` and `@ManyToOne` associations. Accessing an unloaded lazy association
outside of a transaction triggers a `LazyInitialisationException` or fires an unexpected
SQL query, which can:
- Cause subtle bugs in logging statements
- Trigger N+1 queries in test output or debug logging
- Fail with an exception if no active transaction exists

### 3. `@RequiredArgsConstructor` (included in `@Data`) — unnecessary for Ebean

Ebean does not require a default constructor — it can construct entity instances without
one. `@RequiredArgsConstructor` therefore adds nothing useful to entity beans.

---

## Recommended Annotation Set

Use exactly these three Lombok annotations on every Ebean entity bean:

```java
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "my_table")
public class MyEntity {
    // ...
}
```

| Annotation | Purpose |
|---|---|
| `@Getter` | Generates `getFoo()` / `isFoo()` accessor methods |
| `@Setter` | Generates `setFoo(value)` mutator methods; Ebean enhancement intercepts these for dirty tracking |
| `@Accessors(chain = true)` | Makes setters return `this`, enabling fluent/builder-style property setting |

---

## `@Accessors(chain = true)` — Fluent Setter Style

With `chain = true`, setters return `this` instead of `void`, allowing method chaining:

```java
// without chain = true (void setters)
CMachine machine = new CMachine();
machine.setMake("Toyota");
machine.setModel("Hilux");
machine.setStatus("active");

// with @Accessors(chain = true)
CMachine machine = new CMachine()
    .setMake("Toyota")
    .setModel("Hilux")
    .setStatus("active");
```

This is particularly useful when building test data:

```java
CMachine machine = new CMachine()
    .setGid(UUID.randomUUID())
    .setMachineType("HV")
    .setStatus("active")
    .setMake("Komatsu")
    .setModel("PC200");

database.save(machine);
```

Ebean's bytecode enhancement is fully compatible with chained setters — the
enhancement intercepts each `setFoo()` call to record which fields have been modified
(dirty checking), regardless of whether the setter returns `void` or `this`.

---

## `@Accessors(fluent = true)` — also compatible

`@Accessors(fluent = true)` removes the `get`/`set`/`is` prefix, generating `name()`
(getter) and `name(value)` (setter) instead of `getName()` and `setName(value)`.

Ebean does **not** require JavaBeans naming conventions — it can work with any accessor
method style, including fluent accessors with no prefix. `@Accessors(fluent = true)` is
therefore compatible with Ebean.

`@Accessors(chain = true)` is the more common choice in practice (it keeps the familiar
`get`/`set` prefix while adding method chaining), but `fluent = true` is a valid
alternative if that style is preferred consistently across the codebase.

---

## Full Entity Bean Example

```java
package com.example.repository.data;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "machine")
public class CMachine {

    @Id
    private long id;

    @Version
    private int version;

    @Column(nullable = false, unique = true)
    private UUID gid;

    @Column(nullable = false, length = 10)
    private String machineType;

    @Column(length = 200)
    private String make;

    @Column(length = 200)
    private String model;

    @WhenCreated
    private Instant created;

    @WhenModified
    private Instant lastModified;
}
```

---

## Summary: Lombok Annotations and Ebean Compatibility

| Lombok Annotation | Compatible? | Notes                                                                                                                                                             |
|---|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Getter` | ✅ Safe | Use on every entity bean                                                                                                                                          |
| `@Setter` | ✅ Safe | Use on every entity bean; enhancement intercepts these                                                                                                            |
| `@Accessors(chain = true)` | ✅ Safe | Recommended for fluent construction style                                                                                                                         |
| `@ToString` | ❌ Avoid | Ebean does a better job and handles recursion                                                                                                                     |
| `@EqualsAndHashCode` | ❌ Avoid | Breaks entity identity and `@Id`-based equality                                                                                                                   |
| `@Data` | ❌ Avoid | Includes `@EqualsAndHashCode` and `@ToString` — both problematic                                                                                                  |
| `@Value` | ❌ Avoid | Makes fields final — incompatible with Ebean's field-level bytecode enhancement                                                                                   |
| `@Accessors(fluent = true)` | ✅ Safe | Removes `get`/`set` prefix — Ebean does not require JavaBeans naming conventions and works with any accessor style                                                |
| `@Builder` | ⚠️ Careful | Usable on non-entity helper/factory classes; on entity beans it requires a no-arg constructor alongside it and offers no advantage over `@Accessors(chain = true)` |

---

## Relationship with Ebean Bytecode Enhancement

Ebean's bytecode enhancement (applied by `ebean-maven-plugin` at build time) modifies
the `setXxx()` methods of entity beans to:
1. Mark the field as dirty (changed) so only modified fields are included in UPDATE statements
2. Support lazy loading of associations when a getter is called on an unloaded field

For this to work correctly, Ebean needs:
- Accessor methods for each persistent field (any naming style is fine — `getFoo()`, `foo()`, or no accessors at all; Ebean can also access fields directly)
- No override of `hashCode()` / `equals()` that would interfere with the identity map — which means **no `@Data` or `@EqualsAndHashCode`**
