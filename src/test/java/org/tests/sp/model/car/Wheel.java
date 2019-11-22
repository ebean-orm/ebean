package org.tests.sp.model.car;

import org.tests.sp.model.IdEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "sp_car_wheel")
public class Wheel extends IdEntity {

  private static final long serialVersionUID = 2399600193947163469L;

  private String name;

  public Wheel(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
