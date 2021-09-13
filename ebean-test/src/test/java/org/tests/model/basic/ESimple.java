package org.tests.model.basic;

import javax.persistence.*;

@Entity
public class ESimple {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "usertypeid", unique = true, nullable = false)
  private Integer id;

  private String name;

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
