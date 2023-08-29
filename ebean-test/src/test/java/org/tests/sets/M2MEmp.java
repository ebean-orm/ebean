package org.tests.sets;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
public class M2MEmp {

  @Id
  private UUID id;

  private final String code;

  private String name;

  @ManyToMany
  @JoinTable(name="m2m_dept_emp",
          joinColumns=
          @JoinColumn(name="employee_id", referencedColumnName="id"),
          inverseJoinColumns=
          @JoinColumn(name="department_id", referencedColumnName="id")
  )
  private final Set<M2MDepart> departments = new HashSet<>();

  public M2MEmp(String name, String code) {
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

  public Set<M2MDepart> getDepartments() {
    return departments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    M2MEmp employee = (M2MEmp) o;
    return Objects.equals(code, employee.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }
}
