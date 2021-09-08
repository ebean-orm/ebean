package misc.migration.v1_1;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_oto_master")
public class OtoMaster {

  @Id
  Long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master")
  OtoChild child;

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

  public OtoChild getChild() {
    return child;
  }

  public void setChild(OtoChild child) {
    this.child = child;
  }

}
