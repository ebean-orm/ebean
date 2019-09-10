package org.tests.model.basic;

import io.ebean.annotation.Encrypted;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDate;

@Entity
@Table(name = "e_basicenc_client")
public class EBasicEncryptClient {

  public enum Status {
    ONE,
    TWO
  }

  @Id
  long id;

  String name;

  @Encrypted(dbLength = 80, dbEncryption = false)
  String description;

  @Encrypted(dbLength = 20, dbEncryption = false)
  LocalDate dob;

  @Enumerated(EnumType.ORDINAL)
  @Encrypted(dbLength = 20, dbEncryption = false)
  Status status;

  @Version
  long version;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getDob() {
    return dob;
  }

  public void setDob(LocalDate dob) {
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

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
