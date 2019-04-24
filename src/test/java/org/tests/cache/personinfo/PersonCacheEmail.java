package org.tests.cache.personinfo;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

@Entity
@Cache(region = "email")
public class PersonCacheEmail {

  @Id
  @Size(max=128)
  private String id;

  @ManyToOne
  private PersonCacheInfo personInfo;

  private String email;

  public PersonCacheEmail(String id, String email) {
    this.id = id;
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PersonCacheInfo getPersonInfo() {
    return personInfo;
  }

  public void setPersonInfo(PersonCacheInfo personInfo) {
    this.personInfo = personInfo;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
