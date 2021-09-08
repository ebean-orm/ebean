package org.tests.o2m.lazy;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oml_foo")
public class OmlFoo {

  @Id
  private Long id;

  @ManyToOne(optional = false)
  private OmlBar bar;

  @OneToMany(mappedBy = "foo", cascade = CascadeType.ALL)
  private List<OmlBaz> bazList = new ArrayList<OmlBaz>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OmlBar getBar() {
    return bar;
  }

  public void setBar(OmlBar bar) {
    this.bar = bar;
  }

  public List<OmlBaz> getBazList() {
    return bazList;
  }

  public void setBazList(List<OmlBaz> bazList) {
    this.bazList = bazList;
  }

}
