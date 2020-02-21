package misc.migration.v1_2;

import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static io.ebean.annotation.Platform.MYSQL;
import static io.ebean.annotation.Platform.POSTGRES;

@Index(columnNames = "name", platforms = {MYSQL})
@Index(unique = true, columnNames = "lower(name)", platforms = {POSTGRES})
@Index(unique = true, columnNames = "name", platforms = {MYSQL})
@Entity
@Table(name = "migtest_oto_master")
public class OtoMaster {

  @Id
  Long id;

  String name;

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

}
