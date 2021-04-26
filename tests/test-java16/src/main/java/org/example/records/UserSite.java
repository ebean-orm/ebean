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

  public UUID getUserId() {
    return userId;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getNote() {
    return note;
  }

  public long getVersion() {
    return version;
  }
}
