package org.tests.cache.personinfo;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Cache
public class PersonCacheInfo {

  @Id
  private String personId;

  private String name;

  public PersonCacheInfo(String personId, String name) {
    this.personId = personId;
    this.name = name;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
