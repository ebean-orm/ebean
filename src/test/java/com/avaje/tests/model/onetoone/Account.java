package com.avaje.tests.model.onetoone;

import com.avaje.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="oto_account")
public class Account extends BaseModel {

  public static final Find<Long,Account> find = new Find<Long,Account>(){};
  
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
