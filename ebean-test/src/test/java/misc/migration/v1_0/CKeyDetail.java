package misc.migration.v1_0;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_ckey_detail")
public class CKeyDetail {

  @Id
  Integer id;

  @Column(length = 255)
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
