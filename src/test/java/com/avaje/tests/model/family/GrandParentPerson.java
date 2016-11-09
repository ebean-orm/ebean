
package com.avaje.tests.model.family;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.Formula;
import com.avaje.tests.model.basic.EBasic;

@Entity
public class GrandParentPerson extends InheritablePerson {

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<ParentPerson> children = new ArrayList<>();

  // This rather complex formulas should be built later by CustomAnnotationParser
  private static final String PARENT_PERSON_AGGREGATE_JOIN = "left join "
      + "(select parent_identifier, count(*) as child_count, sum(age) as child_age from parent_person group by parent_identifier) "
      + "as f1 on f1.parent_identifier = ${ta}.identifier";

  //@Count("children")
  @Formula(select = "coalesce(f1.child_count, 0)",  join = PARENT_PERSON_AGGREGATE_JOIN )
  private Integer childCount;

  //@Sum("children.age")
  @Formula(select = "coalesce(f1.child_age, 0)",  join = PARENT_PERSON_AGGREGATE_JOIN )
  private Integer totalAge;

  private String familyName;

  private String address;


  @Formula(select = "${ta}.some_bean_id+1")
  @ManyToOne
  private EBasic effectiveBean;
  
  public List<ParentPerson> getChildren() {
    return children;
  }

  public void setChildren(List<ParentPerson> children) {
    this.children = children;
  }


  public Integer getTotalAge() {
    return totalAge;
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

  public Integer getChildCount() {
    return childCount;
  }

  public EBasic getEffectiveBean() {
    return effectiveBean;
  }
}
