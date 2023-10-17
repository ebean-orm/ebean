package misc.migration.v1_1;

import io.ebean.annotation.Index;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static io.ebean.annotation.Platform.POSTGRES;

@Index(columnNames = "name", platforms = POSTGRES)
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
