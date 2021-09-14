package org.tests.model.bridge;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Version;
import java.util.UUID;

@Entity
@IdClass(BDManyId.class)
public class BSiteUserDMany {

  @Id
  private UUID siteId;
  @Id
  private UUID userId;
  @Id
  String name;

  String many;

  @Version
  long version;

  public UUID siteId() {
    return siteId;
  }

  public BSiteUserDMany siteId(UUID siteId) {
    this.siteId = siteId;
    return this;
  }

  public UUID userId() {
    return userId;
  }

  public BSiteUserDMany userId(UUID userId) {
    this.userId = userId;
    return this;
  }

  public String name() {
    return name;
  }

  public BSiteUserDMany name(String name) {
    this.name = name;
    return this;
  }

  public long version() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String many() {
    return many;
  }

  public void setMany(String many) {
    this.many = many;
  }
}
