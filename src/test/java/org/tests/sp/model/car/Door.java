package org.tests.sp.model.car;

import org.tests.sp.model.IdEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "sp_car_door")
public class Door extends IdEntity {

  private static final long serialVersionUID = 2399600193947163469L;

  private String name;

  public Door(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
