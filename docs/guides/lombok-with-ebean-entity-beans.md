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
outside of a transaction triggers a `LazyInitializationException` or fires an unexpected
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
