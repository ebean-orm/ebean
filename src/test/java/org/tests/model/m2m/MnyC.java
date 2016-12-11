package org.tests.model.m2m;

import org.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class MnyC extends BaseModel {

  String name;

  @ManyToMany(mappedBy = "cs")
  List<MnyB> bs;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MnyB> getBs() {
    return bs;
  }

  public void setBs(List<MnyB> bs) {
    this.bs = bs;
  }
}
