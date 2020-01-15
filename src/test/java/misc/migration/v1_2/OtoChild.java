package misc.migration.v1_2;

import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static io.ebean.annotation.Platform.MYSQL;
import static io.ebean.annotation.Platform.POSTGRES;

@Index(name = "ix_m12_otoc71", columnNames = "foo(name)", platforms = {POSTGRES})
@Index(name = "ix_m12_otoc72", columnNames = "bar(name)", platforms = {MYSQL})
@Index(unique = true, name = "uq_m12_otoc71", columnNames = "uqFoo(name)", platforms = {POSTGRES})
@Index(unique = true, name = "uq_m12_otoc72", columnNames = "uqBar(name)", platforms = {MYSQL})
@Index(columnNames = "name", platforms = POSTGRES)
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
