package org.tests.model.lazywithid;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import io.ebean.common.BeanList;

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
