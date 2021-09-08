package org.tests.update;

import io.ebean.annotation.Index;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "e_person_online")
public class EPersonOnline {

  @Id
  Long id;

  @Index(unique = true)
  @Size(max=127)
  String email;

  boolean onlineStatus;

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

  public boolean isOnlineStatus() {
    return onlineStatus;
  }

  public void setOnlineStatus(boolean onlineStatus) {
    this.onlineStatus = onlineStatus;
  }

  public Instant getWhenUpdated() {
    return whenUpdated;
  }

  public void setWhenUpdated(Instant whenUpdated) {
    this.whenUpdated = whenUpdated;
  }
}
