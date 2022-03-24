package misc.migration.v1_1;

import io.ebean.annotation.Tablespace;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "migtest_mtm_c")
@Tablespace("db2;TESTTS;")
public class MtmChild {

  @Id
  Integer id;

  String name;

  @ManyToMany
  List<MtmMaster> masters;

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

  public List<MtmMaster> getMasters() {
    return masters;
  }

  public void setMasters(List<MtmMaster> masters) {
    this.masters = masters;
  }
}
