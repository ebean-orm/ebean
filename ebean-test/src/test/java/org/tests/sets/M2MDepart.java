package org.tests.sets;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.*;

@Entity
public class M2MDepart {

  @Id
  private UUID id;

  private final String name;

  @ManyToMany
  private final Set<M2MEmp> employees = new LinkedHashSet<>();

  public M2MDepart(String name) {
    this.name = name;
  }

  public void addEmployee(M2MEmp employee) {
    this.employees.add(employee);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Set<M2MEmp> employees() {
    return employees;
  }
}
