package org.tests.model.generated;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
@Entity
public class User {

  @Id
  final Long id;

  @Version
  final Long version;

  @Basic
  final String username;

  public User(String username) {
    this.id = null;
    this.version = null;
    this.username = username;
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public String getUsername() {
    return username;
  }

}
