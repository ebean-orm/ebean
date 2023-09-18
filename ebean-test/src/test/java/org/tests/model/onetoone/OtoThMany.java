package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class OtoThMany extends BaseModel {

  String many;

  @OneToOne(mappedBy = "many", cascade = CascadeType.ALL)
  OtoThOne one;

  public String getMany() {
    return many;
  }

  public void setMany(String many) {
    this.many = many;
  }

  public OtoThOne getOne() {
    return one;
  }

  public void setOne(OtoThOne one) {
    this.one = one;
  }
}
