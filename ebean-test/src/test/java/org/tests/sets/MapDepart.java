package org.tests.sets;

import javax.persistence.*;
import java.util.*;

@Entity
public class MapDepart {

  @Id
  private long id;

  private final String name;

  @MapKey(name="code")
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private final Map<String,MapEmp> employees = new LinkedHashMap<>();

  public MapDepart(String name) {
    this.name = name;
  }

  public void addEmployee(MapEmp employee) {
    this.employees.put(employee.getCode(), employee);
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Map<String,MapEmp> employees() {
    return employees;
  }
}
