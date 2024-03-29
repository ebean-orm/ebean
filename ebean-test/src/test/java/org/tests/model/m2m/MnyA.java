package org.tests.model.m2m;

import org.tests.model.BaseModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
public class MnyA extends BaseModel {

  String name;

  @OneToMany(mappedBy = "a", cascade = CascadeType.REMOVE)
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

