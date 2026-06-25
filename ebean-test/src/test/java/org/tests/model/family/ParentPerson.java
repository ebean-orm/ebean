package org.tests.model.family;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Formula2;
import org.tests.model.basic.EBasic;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class ParentPerson extends InheritablePerson {
  private static final long serialVersionUID = 1L;

  //This rather complex formulas should be built later by CustomAnnotationParser
  private static final String CHILD_PERSON_AGGREGATE_JOIN = "left join "
    + "(select i2.parent_identifier, count(*) as child_count, sum(i2.age) as child_age from child_person i2 group by i2.parent_identifier) "
    + "f2 on f2.parent_identifier = ${ta}.identifier";

  @ManyToOne(cascade = CascadeType.ALL)
  private GrandParentPerson parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @OrderBy("identifier")
  private List<ChildPerson> children = new ArrayList<>();

  //@Count("children")
  @Formula(select = "coalesce(f2.child_count, 0)", join = CHILD_PERSON_AGGREGATE_JOIN)
  private Integer childCount;

  //@Sum("children.age")
  @Formula(select = "coalesce(f2.child_age, 0)", join = CHILD_PERSON_AGGREGATE_JOIN)
  private Integer totalAge;

  private String familyName;

  private String address;

  //@Coalesce({ "familyName", "parent.familyName" })
  @Formula2("coalesce(familyName, parent.familyName)")
  private String effectiveFamilyName;

  //@Formula2 equivalent - logical path-based, joins are auto-detected
  @Formula2("coalesce(familyName, parent.familyName)")
  private String derivedFamilyName;

  // @Transient makes the @Formula2 property opt-in (excluded from queries by default)
  @Transient
  @Formula2("coalesce(familyName, parent.familyName)")
  private String lazyDerivedFamilyName;

  //@Coalesce({ "address", "parent.address" })
  @Formula2("coalesce(address, parent.address)")
  private String effectiveAddress;

  @Formula2("coalesce(someBean.id, parent.someBean.id)")
  @ManyToOne
  private EBasic effectiveBean;

  public GrandParentPerson getParent() {
    return parent;
  }

  public void setParent(GrandParentPerson parent) {
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

  public List<ChildPerson> getChildren() {
    return children;
  }

  public Integer getChildCount() {
    return childCount;
  }

  public Integer getTotalAge() {
    return totalAge;
  }

  public String getEffectiveFamilyName() {
    return effectiveFamilyName;
  }

  public String getDerivedFamilyName() {
    return derivedFamilyName;
  }

  public String getLazyDerivedFamilyName() {
    return lazyDerivedFamilyName;
  }

  public String getEffectiveAddress() {
    return effectiveAddress;
  }

  public EBasic getEffectiveBean() {
    return effectiveBean;
  }

}
