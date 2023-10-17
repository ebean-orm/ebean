package misc.migration.v1_2;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
