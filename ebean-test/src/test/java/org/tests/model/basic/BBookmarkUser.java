package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Chris
 */
@Entity
public class BBookmarkUser {

  @Id
  @GeneratedValue
  private Integer id;

  private String name;

  private String password;

  private String emailAddress;

  private String country;

  /**
   * An optional non-cascading ManyToOne.
   */
  @ManyToOne
  private BBookmarkOrg org;

  public BBookmarkUser(String name) {
    this.name = name;
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getEmailAddress() {
    return this.emailAddress;
  }

  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getCountry() {
    return this.country;
  }

  public void setCountry(final String country) {
    this.country = country;
  }

  public BBookmarkOrg getOrg() {
    return org;
  }

  public void setOrg(BBookmarkOrg org) {
    this.org = org;
  }
}
