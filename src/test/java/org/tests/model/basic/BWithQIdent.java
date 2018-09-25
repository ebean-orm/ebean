package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Entity
public class BWithQIdent {

  @Id
  Integer id;

  @Column(name = "`Name`", unique = true)
  @Size(max = 191) // key must not exceed 767 Bytes, so max key len for mysql with utf8mb4 = 191*4 = 764 bytes
  String name;

  @Column(name = "`CODE`")
  String CODE;

  @Version
  Timestamp lastUpdated;

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

  public String getCODE() {
    return CODE;
  }

  public void setCODE(String CODE) {
    this.CODE = CODE;
  }

  public Timestamp getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

}
