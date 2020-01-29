package misc.migration.v1_0;

import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static io.ebean.annotation.Platform.POSTGRES;

@Index(platforms = POSTGRES, name = "idxd_migtest_0", definition = "create index idxd_migtest_0 on migtest_oto_child using hash (upper(name)) where upper(name) = 'JIM'")
@Index(platforms = POSTGRES, columnNames = {"lower(name)","id"}, concurrent = true)
@Index(platforms = POSTGRES, columnNames = "lower(name)")
@Entity
@Table(name = "migtest_oto_child")
public class OtoChild {

  @Id
  Integer id;

  String name;

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

}
