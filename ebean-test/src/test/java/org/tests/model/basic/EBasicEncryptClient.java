package org.tests.model.basic;

import io.ebean.annotation.DbArray;
import io.ebean.annotation.Encrypted;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

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

  @Encrypted(dbEncryption = false)
  @NotNull
  @DbArray
  Set<String> emailAddresses = new LinkedHashSet<>();

  @Encrypted(dbEncryption = false, dbLength = 128)
  @DbArray(length = 50)
  List<String> tags;

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

  public Set<String> getEmailAddresses() {
    return emailAddresses;
  }

  public void setEmailAddresses(Set<String> emailAddresses) {
    this.emailAddresses = emailAddresses;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
