package com.avaje.tests.model.onetoone;

import com.avaje.ebean.Finder;
import com.avaje.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "oto_user")
public class User extends BaseModel {

  public static final Finder<Long, User> find = new Finder<>(User.class);

  String name;

  @OneToOne(optional = false)
  Account account;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

}
