package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Cache
@Entity
@Inheritance
@DiscriminatorValue("T")
public class CInhTwo extends CInhRoot {
  private static final long serialVersionUID = -8528396890675473212L;

  private String action;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
