package org.example.records;

import io.ebean.Model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class UserRole extends Model {

  @EmbeddedId
  final UserRoleId id;

  String note;

  @Version
  long version;

  public UserRole(UserRoleId id, String note) {
    this.id = id;
    this.note = note;
  }

  public UserRoleId id() {
    return id;
  }

  public String note() {
    return note;
  }

  public long version() {
    return version;
  }
}
