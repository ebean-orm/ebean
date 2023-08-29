package org.tests.sets;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MapEmp2 {

  @Id
  private long id;

  private final String code;

  private String name;

  @ManyToOne
  MapDepart2 department;

  public MapEmp2(String name, String code) {
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

  public void setDepartment(MapDepart2 department) {
    this.department = department;
  }

}
