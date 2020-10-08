package org.tests.model.basic.first;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "f_first")
public class First {

  @Id
  Long id;

  String name;

  @OneToOne(mappedBy = "first", cascade = CascadeType.ALL)
  Second second;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Second getSecond() {
    return second;
  }

  public void setSecond(Second second) {
    this.second = second;
  }

}
