package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_oto_child")
public class OtoChild {

  @Id
  Integer id;

  String name;

  @OneToOne
  OtoMaster master;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OtoMaster getMaster() {
    return master;
  }

  public void setMaster(OtoMaster master) {
    this.master = master;
  }

}
