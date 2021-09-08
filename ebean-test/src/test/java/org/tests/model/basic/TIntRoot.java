package org.tests.model.basic;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance
@DiscriminatorColumn(name = "my_type", length = 3, discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("1")
public class TIntRoot implements Serializable {
  private static final long serialVersionUID = -7057502590435806504L;

  @Id
  Integer id;

  String name;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
