package org.tests.update;

import io.ebean.annotation.Index;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "e_person_online")
public class EPersonOnline {

  @Id
  Long id;

  @Index(unique = true)
  @Size(max=255)
  String email;

  boolean online;

  @WhenModified
  Instant whenUpdated;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public Instant getWhenUpdated() {
    return whenUpdated;
  }

  public void setWhenUpdated(Instant whenUpdated) {
    this.whenUpdated = whenUpdated;
  }
}
