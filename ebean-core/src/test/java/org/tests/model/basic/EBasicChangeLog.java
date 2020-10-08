package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.ChangeLog;
import io.ebean.annotation.ReadAudit;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Cache(enableQueryCache = true)
@ReadAudit
@ChangeLog(updatesThatInclude = {"name", "shortDescription"})
@Entity
public class EBasicChangeLog {

  @Id
  Long id;

  @Size(max = 20)
  String name;

  @Size(max = 50)
  String shortDescription;

  @Size(max = 100)
  String longDescription;

  @WhoCreated
  String whoCreated;

  @WhoModified
  String whoModified;

  @WhenCreated
  Timestamp whenCreated;

  @WhenModified
  Timestamp whenModified;

  @Version
  Long version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  public String getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(String longDescription) {
    this.longDescription = longDescription;
  }

  public String getWhoCreated() {
    return whoCreated;
  }

  public void setWhoCreated(String whoCreated) {
    this.whoCreated = whoCreated;
  }

  public String getWhoModified() {
    return whoModified;
  }

  public void setWhoModified(String whoModified) {
    this.whoModified = whoModified;
  }

  public Timestamp getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Timestamp whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Timestamp getWhenModified() {
    return whenModified;
  }

  public void setWhenModified(Timestamp whenModified) {
    this.whenModified = whenModified;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
