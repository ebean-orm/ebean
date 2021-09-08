package org.tests.model.pview;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "wview")
public class Wview {

  @Id
  @Column(name = "id")
  private UUID id;

  @Basic(optional = false)
  @Column(unique = true)
  @Size(max=127)
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
