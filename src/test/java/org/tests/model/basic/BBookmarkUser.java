package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * represents a user entity. A user contains a username and password.
 *
 * @author Chris
 */
@Entity
public class BBookmarkUser {

  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private String name;

  @Column
  private String password;

  @Column
  private String emailAddress;

  @Column
  private String country;

//	@Version
//	private Timestamp lastUpdate;

  /**
   * @return the id
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * @param id the id to set
   */
  public void setId(final Integer id) {
    this.id = id;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return the emailAddress
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }

  /**
   * @param emailAddress the emailAddress to set
   */
  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return this.country;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(final String country) {
    this.country = country;
  }

//	public Timestamp getLastUpdate() {
//    	return lastUpdate;
//    }
//
//	public void setLastUpdate(Timestamp lastUpdate) {
//    	this.lastUpdate = lastUpdate;
//    }

}
