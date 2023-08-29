package org.tests.sets;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class MapEmp {

  @Id
  private long id;

  private final String code;

  private String name;

  @ManyToOne
  MapDepart department;

  public MapEmp(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public void setDepartment(MapDepart department) {
    this.department = department;
  }

}
