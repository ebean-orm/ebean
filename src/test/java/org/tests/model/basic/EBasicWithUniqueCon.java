package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import java.sql.Timestamp;

@Entity
@Table(name = "e_basicverucon")
@UniqueConstraint(columnNames = {"other", "other_one"})
public class EBasicWithUniqueCon {

  @Id
  Integer id;

  @Column(unique = true)
  @Size(max=127)
  String name;

  @Size(max=127)
  String other;

  @Size(max=127)
  String otherOne;

  String description;

  @Version
  Timestamp lastUpdate;

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

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  public String getOtherOne() {
    return otherOne;
  }

  public void setOtherOne(String otherOne) {
    this.otherOne = otherOne;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Timestamp getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Timestamp lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

}
