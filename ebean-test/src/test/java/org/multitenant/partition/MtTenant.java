package org.multitenant.partition;

import io.ebean.annotation.Length;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class MtTenant {

  @Id
  final String id;

  @Length(50)
  String name;

  @Length(50)
  String email;

  String notes;

  @Version
  long version;

  public MtTenant(String id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
