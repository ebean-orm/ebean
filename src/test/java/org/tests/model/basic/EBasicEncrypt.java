package org.tests.model.basic;

import io.ebean.annotation.Encrypted;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "e_basicenc")
public class EBasicEncrypt {

  public enum Status {
    ONE,
    TWO
  }

  @Id
  Integer id;

  String name;

  //@Lob
  @Encrypted(dbLength = 80)
  String description;

  @Encrypted(dbLength = 20)
  Date dob;

  @Enumerated(EnumType.ORDINAL)
  @Encrypted(dbLength = 20)
  Status status;

  //@Version
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

  public Date getDob() {
    return dob;
  }

  public void setDob(Date dob) {
    this.dob = dob;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
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
