package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class OtoThOne extends BaseModel {

  boolean one;

  @OneToOne
  OtoThMany many;

  public boolean isOne() {
    return one;
  }

  public void setOne(boolean one) {
    this.one = one;
  }

  public OtoThMany getMany() {
    return many;
  }

  public void setMany(OtoThMany many) {
    this.many = many;
  }
}
