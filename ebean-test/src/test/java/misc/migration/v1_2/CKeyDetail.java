package misc.migration.v1_2;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_ckey_detail")
public class CKeyDetail {

  @Id
  Integer id;

  String something;


  public CKeyDetail() {

  }

  public CKeyDetail(String something) {
    this.something = something;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getSomething() {
    return something;
  }

  public void setSomething(String something) {
    this.something = something;
  }

}
