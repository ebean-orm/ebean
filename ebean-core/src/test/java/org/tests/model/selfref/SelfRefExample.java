package org.tests.model.selfref;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.List;

@Entity
public class SelfRefExample {

  @Id
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  private SelfRefExample parent;

  @OrderBy("id")
  @OneToMany(mappedBy = "parent")
  private List<SelfRefExample> children;

  public SelfRefExample(String name, SelfRefExample parent) {
    this.name = name;
    this.parent = parent;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return this.id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the parent
   */
  public SelfRefExample getParent() {
    return this.parent;
  }

  /**
   * @param parent the parent to set
   */
  public void setParent(SelfRefExample parent) {
    this.parent = parent;
  }

  /**
   * @return the children
   */
  public List<SelfRefExample> getChildren() {
    return this.children;
  }

  /**
   * @param children the children to set
   */
  public void setChildren(List<SelfRefExample> children) {
    this.children = children;
  }
}
