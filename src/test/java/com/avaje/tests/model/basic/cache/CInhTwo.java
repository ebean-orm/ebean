package com.avaje.tests.model.basic.cache;

import com.avaje.ebean.annotation.CacheStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@CacheStrategy
@Entity
@Inheritance
@DiscriminatorValue("T")
public class CInhTwo extends CInhRoot {

  private String action;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
