package org.tests.level;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
public class Level1 {
  @Id
  Long id;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
    name = "level1_level4",
    joinColumns = @JoinColumn(name = "level1_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "level4_id", referencedColumnName = "id")
  )
  Set<Level4> level4s;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
    name = "level1_level2",
    joinColumns = @JoinColumn(name = "level1_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "level2_id", referencedColumnName = "id")
  )
  Set<Level2> level2s;

  String name;

  public Level1(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Set<Level2> getLevel2s() {
    return level2s;
  }

  public Set<Level4> getLevel4s() {
    return level4s;
  }
}
