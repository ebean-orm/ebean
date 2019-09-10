package org.tests.model.family;

import io.ebean.annotation.Formula;
import org.tests.model.basic.EBasic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GrandParentPerson extends InheritablePerson {
  private static final long serialVersionUID = 1L;
  
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @OrderBy("identifier")
  private List<ParentPerson> children = new ArrayList<>();

  // This rather complex formulas should be built later by CustomAnnotationParser
  private static final String PARENT_PERSON_AGGREGATE_JOIN = "left join "
    + "(select i1.parent_identifier, count(*) as child_count, sum(i1.age) as child_age from parent_person i1 group by i1.parent_identifier) "
    + "f1 on f1.parent_identifier = ${ta}.identifier";

  //@Count("children")
  @Formula(select = "coalesce(f1.child_count, 0)", join = PARENT_PERSON_AGGREGATE_JOIN)
  private Integer childCount;

  //@Sum("children.age")
  @Formula(select = "coalesce(f1.child_age, 0)", join = PARENT_PERSON_AGGREGATE_JOIN)
  private Integer totalAge;

  private String familyName;

  private String address;


  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  @Formula(select = "f3.id", join = "left join e_basic f3 on f3.name = ${ta}.family_name")
  private EBasic basicSameName;
  
  
  // Demonstrate formula usage
  @Formula(select = "coalesce(${ta}.some_bean_id,1)")
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
  
  public EBasic getBasicSameName() {
    return basicSameName;
  }
  
  public void setBasicSameName(EBasic basicSameName) {
    this.basicSameName = basicSameName;
  }
}
