package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class SelfManyMany {

  @Id
  private long id;

  private final String name;

  @ManyToMany
  // requires explicit @JoinTable
  @JoinTable(name = "self_many_bridge",
    joinColumns = @JoinColumn(name = "self_1_id"),
    inverseJoinColumns = @JoinColumn(name = "self_2_id"))
  private List<SelfManyMany> related;

  public SelfManyMany(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<SelfManyMany> getRelated() {
    return related;
  }
}
