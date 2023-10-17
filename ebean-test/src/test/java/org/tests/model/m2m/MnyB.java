package org.tests.model.m2m;

import io.ebean.annotation.Identity;
import org.tests.model.BaseModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.List;

import static io.ebean.annotation.IdentityGenerated.BY_DEFAULT;

@Identity(generated = BY_DEFAULT)
@Entity
public class MnyB extends BaseModel {

  String name;

  @ManyToOne
  MnyA a;

  @ManyToMany(cascade = CascadeType.REMOVE)
  List<MnyC> cs;

  public MnyB(String name) {
    this.name = name;
  }

  public MnyB() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MnyA getA() {
    return a;
  }

  public void setA(MnyA a) {
    this.a = a;
  }

  public List<MnyC> getCs() {
    return cs;
  }

  public void setCs(List<MnyC> cs) {
    this.cs = cs;
  }
}
