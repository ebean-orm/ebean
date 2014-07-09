package com.avaje.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.avaje.tests.model.BaseModel;

@Entity
@Table(name="oto_account")
public class Account extends BaseModel {

  public static final Finder<Long,Account> find = new Finder<Long,Account>(Long.class, Account.class);
  
  String name;
  
  @OneToOne(mappedBy = "account",optional = true)
  User user;

  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
  
}
