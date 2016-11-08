package com.avaje.tests.model.pview;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "wview")
public class Wview {

  @Id
  @Column(name = "id")
  private UUID id;

  @Basic(optional = false)
  @Column(unique = true)
  private String name;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
