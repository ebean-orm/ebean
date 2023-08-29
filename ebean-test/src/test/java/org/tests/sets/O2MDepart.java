package org.tests.sets;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class O2MDepart {

  @Id
  private UUID id;

  private final String name;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<O2MEmp> employees = new LinkedHashSet<>();

  public O2MDepart(String name) {
    this.name = name;
  }

  public void addEmployee(O2MEmp employee) {
    this.employees.add(employee);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Set<O2MEmp> employees() {
    return employees;
  }
}
