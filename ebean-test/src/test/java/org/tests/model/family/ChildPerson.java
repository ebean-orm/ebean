package org.tests.model.family;

import io.ebean.annotation.Formula;
import org.tests.model.basic.EBasic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ChildPerson extends InheritablePerson {
  private static final long serialVersionUID = 1L;

  private static final String PARENTS_JOIN = "join parent_person j1 on j1.identifier = ${ta}.parent_identifier "
    + "join grand_parent_person j2 on j2.identifier = j1.parent_identifier";

  @ManyToOne(cascade = CascadeType.ALL)
  private ParentPerson parent;

  private String familyName;

  private String address;

  //@Coalesce({ "familyName", "parent.familyName", "parent.parent.familyName" })
  @Formula(select = "coalesce(${ta}.family_name, j1.family_name, j2.family_name)", join = PARENTS_JOIN)
  private String effectiveFamilyName;

  //@Coalesce({ "address", "parent.address", "parent.parent.address"  })
  @Formula(select = "coalesce(${ta}.address, j1.address, j2.address)", join = PARENTS_JOIN)
  private String effectiveAddress;

  @Formula(select = "coalesce(${ta}.some_bean_id, j1.some_bean_id, j2.some_bean_id)", join = PARENTS_JOIN)
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

  public String getEffectiveAddress() {
    return effectiveAddress;
  }

  public EBasic getEffectiveBean() {
    return effectiveBean;
  }

}
