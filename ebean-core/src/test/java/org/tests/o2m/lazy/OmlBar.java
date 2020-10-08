package org.tests.o2m.lazy;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oml_bar")
public class OmlBar {

  @Id
  private Long id;

  @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL)
  private List<OmlFoo> fooList = new ArrayList<OmlFoo>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<OmlFoo> getFooList() {
    return fooList;
  }

  public void setFooList(List<OmlFoo> fooList) {
    this.fooList = fooList;
  }

}
