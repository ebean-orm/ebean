package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class OtoThTop extends BaseModel {

  String top;

  @OneToMany(cascade = CascadeType.ALL)
  List<OtoThMany> manies;

  public String getTop() {
    return top;
  }

  public void setTop(String top) {
    this.top = top;
  }

  public List<OtoThMany> getManies() {
    return manies;
  }

  public void setManies(List<OtoThMany> manies) {
    this.manies = manies;
  }
}
