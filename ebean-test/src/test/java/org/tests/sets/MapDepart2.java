package org.tests.sets;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class MapDepart2 {

  @Id
  private long id;

  private final String name;

  @MapKey(name="code")
  @OneToMany(cascade = CascadeType.ALL) // NO ORPHAN REMOVAL
  private final Map<String,MapEmp2> employees = new LinkedHashMap<>();

  public MapDepart2(String name) {
    this.name = name;
  }

  public void addEmployee(MapEmp2 employee) {
    this.employees.put(employee.getCode(), employee);
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Map<String,MapEmp2> employees() {
    return employees;
  }
}
