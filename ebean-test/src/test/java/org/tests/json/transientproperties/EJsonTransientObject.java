package org.tests.json.transientproperties;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Sql
@Entity
public class EJsonTransientObject {

  @Id
  private Long id;

  private String name;

  @Transient
  private Boolean basic;

  @Transient
  private SomeBean someBean;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getBasic() {
    return basic;
  }

  public void setBasic(Boolean basic) {
    this.basic = basic;
  }

  public SomeBean getSomeBean() {
    return someBean;
  }

  public void setSomeBean(SomeBean someBean) {
    this.someBean = someBean;
  }

  public static class SomeBean {

    public String name;

    public String baz;

  }
}
