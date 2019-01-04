package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class EsdMaster extends BaseSoftDelete {

  String name;

  @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
  List<EsdDetail> details;

  public EsdMaster(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<EsdDetail> getDetails() {
    return details;
  }

  public void setDetails(List<EsdDetail> details) {
    this.details = details;
  }
}
