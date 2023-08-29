package org.tests.sets;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
public class O2MEmp {

  @Id
  private UUID id;

  private final String code;

  private String name;

  @ManyToOne
  O2MDepart department;

  public O2MEmp(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public void setDepartment(O2MDepart department) {
    this.department = department;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    O2MEmp employee = (O2MEmp) o;
    return Objects.equals(code, employee.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

}
