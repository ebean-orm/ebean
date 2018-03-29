package misc.migration.v1_0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_ckey_assoc")
public class CKeyAssoc {

  @Id
  Integer id;

  String assocOne;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAssocOne() {
    return assocOne;
  }

  public void setAssocOne(String assocOne) {
    this.assocOne = assocOne;
  }

}
