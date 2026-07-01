package org.tests.model.family;

import io.ebean.annotation.Formula2;
import org.tests.model.basic.EBasic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ChildPerson extends InheritablePerson {
  private static final long serialVersionUID = 1L;

  @ManyToOne(cascade = CascadeType.ALL)
  private ParentPerson parent;

  private String familyName;

  private String address;

  @Formula2("coalesce(familyName, parent.familyName, parent.parent.familyName)")
  private String effectiveFamilyName;

  @Formula2("coalesce(familyName, parent.familyName, parent.parent.familyName)")
  private String derivedFamilyName;

  @Formula2("coalesce(address, parent.address, parent.parent.address)")
  private String effectiveAddress;

  @Formula2("coalesce(someBean.id, parent.someBean.id, parent.parent.someBean.id)")
  @ManyToOne
  private EBasic effectiveBean;

  public ParentPerson getParent() {
    return parent;
  }

  public void setParent(ParentPerson parent) {
    this.parent = parent;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getEffectiveFamilyName() {
    return effectiveFamilyName;
  }

  public String getDerivedFamilyName() {
    return derivedFamilyName;
  }

  public String getEffectiveAddress() {
    return effectiveAddress;
  }

  public EBasic getEffectiveBean() {
    return effectiveBean;
  }

}
