package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
public class OtoThTop extends BaseModel {

  String topp;

  @OneToMany(cascade = CascadeType.ALL)
  List<OtoThMany> manies;

  public String getTopp() {
    return topp;
  }

  public void setTopp(String topp) {
    this.topp = topp;
  }

  public List<OtoThMany> getManies() {
    return manies;
  }

  public void setManies(List<OtoThMany> manies) {
    this.manies = manies;
  }
}
