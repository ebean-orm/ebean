package org.tests.model.family;

import io.ebean.annotation.Formula;
import org.tests.model.basic.EBasic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;


@Entity
public class ParentPerson extends InheritablePerson {
  private static final long serialVersionUID = 1L;

  //This rather complex formulas should be built later by CustomAnnotationParser
  private static final String CHILD_PERSON_AGGREGATE_JOIN = "left join "
    + "(select i2.parent_identifier, count(*) as child_count, sum(i2.age) as child_age from child_person i2 group by i2.parent_identifier) "
    + "f2 on f2.parent_identifier = ${ta}.identifier";

  private static final String GRAND_PARENT_PERSON_JOIN = "join grand_parent_person j1 on j1.identifier = ${ta}.parent_identifier";

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
  @Formula(select = "coalesce(${ta}.family_name, j1.family_name)", join = GRAND_PARENT_PERSON_JOIN)
  private String effectiveFamilyName;

  //@Coalesce({ "address", "parent.address" })
  @Formula(select = "coalesce(${ta}.address, j1.address)", join = GRAND_PARENT_PERSON_JOIN)
  private String effectiveAddress;

  @Formula(select = "coalesce(${ta}.some_bean_id, j1.some_bean_id)", join = GRAND_PARENT_PERSON_JOIN)
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

  public String getEffectiveAddress() {
    return effectiveAddress;
  }

  public EBasic getEffectiveBean() {
    return effectiveBean;
  }

}
