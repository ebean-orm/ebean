package org.tests.model.lazywithid;

import io.ebean.common.BeanList;

import javax.persistence.*;
import java.util.List;

@Entity
public class Tune {

  @Id
  @Column(name = "id")
  Long _id;

  String name;

  @OneToMany(cascade = CascadeType.ALL)
  private List<Looney> loonies = new BeanList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Looney> getLoonies() {
    return loonies;
  }

  public void setLoonies(final List<Looney> loonies) {
    this.loonies = loonies;
  }
}
