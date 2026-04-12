# Entity Bean Creation Guide for AI Agents

**Target Audience:** AI systems (Claude, Copilot, ChatGPT, etc.)
**Purpose:** Learn how to generate clean, idiomatic Ebean entity beans
**Key Insight:** Ebean requires private fields with getters/setters (or other accessors), but they don't need to follow Java bean conventions; no public fields; no equals/hashCode implementation needed
**Language:** Java
**Framework:** Ebean ORM

---

## Quick Rules

Before writing entity code, remember:

| Requirement | Needed? | Notes                                                                                                                 |
|-------------|---------|-----------------------------------------------------------------------------------------------------------------------|
| `@Entity` annotation | ✅ **YES** | Marks class as persistent entity                                                                                      |
| `@Id` annotation | ✅ **YES** | Marks primary key field                                                                                               |
| Getters/setters (or other accessors) | ✅ **YES** | Required for field access. Don't need to follow Java bean spec; can be record-style, fluent, or any accessor pattern. |
| Default constructor | ❌ **NO** | Not required. Ebean can instantiate without it.                                                                       |
| equals/hashCode | ❌ **NO** | Ebean auto-enhances these at compile time.                                                                            |
| toString() | ❌ **NO** | Ebean auto-enhances this. Don't implement with getters.                                                               |
| `@Version` | ⚠️ **OPTIONAL** | Use for optimistic locking. Highly recommended.                                                                       |
| `@WhenCreated` | ⚠️ **OPTIONAL** | Auto-timestamp creation time. Highly recommended. Use for audit trail.                                                |
| `@WhenModified` | ⚠️ **OPTIONAL** | Auto-timestamp modification time. Highly recommended. Use for audit trail.                                            |

**Critical:**
- Prefer primitive `long` for `@Id` and `@Version`, NOT `Long` object.
- Fields should be **private** or **protected** (not public). Access via getters/setters or other accessors.
- Getters/setters do NOT need to follow Java bean conventions.

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
- ✅ Getters/setters don't need to follow Java bean conventions (shown above with setName but no getId setter)
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
DB.save(order);  // createdAt is automatically set by Ebean

// Modify
order.setTotalAmount(new BigDecimal("109.99"));
DB.update(order);  // version incremented, modifiedAt updated automatically

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

## Pattern 5: Entity with Getters/Setters (Optional)

**Use this when:** External code accesses this entity's fields.

```java
@Entity
public class Employee {
  @Id long id;
  @Version long version;

  String firstName;
  String lastName;
  BigDecimal salary;

  public long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public BigDecimal getSalary() {
    return salary;
  }

  public void setSalary(BigDecimal salary) {
    this.salary = salary;
  }
}
```

**Note:** This is valid but verbose. Most Ebean code doesn't use getters/setters. **Only add them if needed by your API design.**

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
- `uuid` (UUID) ✅ Use this

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
}
```

Each step adds only what's necessary. No getters/setters needed unless your design requires them.

---

## Usage Examples

### Creating and saving
```java
Customer customer = new Customer();
customer.name = "Alice";
DB.save(customer);  // id auto-generated
```

### Finding
```java
Customer found = DB.find(Customer.class, 1);
System.out.println(found.name);  // Direct field access works
```

### Updating
```java
found.name = "Bob";
DB.update(found);  // version auto-incremented
```

### Collections (relationships)
```java
Customer customer = DB.find(Customer.class, 1);
List<Order> orders = customer.orders;  // Lazy loads automatically
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
- Add getters/setters only if your API requires them

❌ **DON'T:**
- Use Long object for @Id/@Version
- Implement equals/hashCode
- Implement toString() with getters
- Use Set for @OneToMany/@ManyToMany
- Add unnecessary @Column annotations
- Add getters/setters "just in case"
- Add default constructors "just in case"

**Result:** Clean, readable, maintainable entity beans with full ORM functionality and zero boilerplate.

---

## Related Documentation

- Entity Bean Best Practices: `/docs/best-practice/`
- JPA Mapping Reference: `/docs/mapping/jpa/`
- Ebean Extensions: `/docs/mapping/extensions/`
- First Entity Guide: `/docs/intro/first-entity/`
