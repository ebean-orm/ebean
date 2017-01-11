package org.tests.model.generated;

import javax.persistence.*;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
@Entity
@Table(name = "g_user")
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
