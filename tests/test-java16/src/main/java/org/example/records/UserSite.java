package org.example.records;

import io.ebean.Model;

import javax.persistence.*;
import java.util.UUID;

@IdClass(UserSiteId.class)
@Entity
public class UserSite extends Model {

  @Id
  final UUID userId;

  @Id
  final UUID siteId;

  String note;

  @Version
  long version;

  public UserSite(UUID userId, UUID siteId) {
    this.userId = userId;
    this.siteId = siteId;
  }

  public UUID userId() {
    return userId;
  }

  public UUID siteId() {
    return siteId;
  }

  public void note(String note) {
    this.note = note;
  }

  public void version(long version) {
    this.version = version;
  }

  public String note() {
    return note;
  }

  public long version() {
    return version;
  }
}
