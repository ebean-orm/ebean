package org.tests.level;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class Level2 {
  @Id
  Long id;

  @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinTable(
    name = "level2_level3",
    joinColumns = @JoinColumn(name = "level2_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "level3_id", referencedColumnName = "id")
  )
  List<Level3> level3s;

  String name;

  public Level2(String name) {
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

  public List<Level3> getLevel3s() {
    return level3s;
  }
}
