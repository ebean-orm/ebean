package org.tests.model.generated;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
@Entity
@Table(name = "g_who_props_otm")
public class WhoPropsOneToMany {

  @Id
  Long id;

  @Version
  Long version;

  @WhenCreated
  Timestamp whenCreated;

  @WhenModified
  Timestamp whenModified;

  @ManyToOne
  @WhoCreated
  User whoCreated;

  @ManyToOne
  @WhoModified
  User whoModified;

  @Basic
  String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
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

  public User getWhoCreated() {
    return whoCreated;
  }

  public void setWhoCreated(User whoCreated) {
    this.whoCreated = whoCreated;
  }

  public User getWhoModified() {
    return whoModified;
  }

  public void setWhoModified(User whoModified) {
    this.whoModified = whoModified;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
