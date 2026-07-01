package org.tests.model.basic;

import io.ebean.annotation.Sql;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import org.tests.model.composite.CkeUser;

@Entity
@Sql
public class OtherAggregate {

  @OneToOne
  CkeUser user;

  int totalContacts;

  public CkeUser getUser() {
    return user;
  }

  public void setUser(CkeUser user) {
    this.user = user;
  }

  public int getTotalContacts() {
    return totalContacts;
  }

  public void setTotalContacts(int totalContacts) {
    this.totalContacts = totalContacts;
  }
}
