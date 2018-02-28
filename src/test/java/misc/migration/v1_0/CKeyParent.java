package misc.migration.v1_0;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "migtest_ckey_parent")
public class CKeyParent {

  @EmbeddedId
  CKeyParentId id;

  String name;

  @Version
  int version;

  public CKeyParentId getId() {
    return id;
  }

  public void setId(CKeyParentId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
