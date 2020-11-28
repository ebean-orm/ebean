package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_ckey_detail")
public class CKeyDetail {

  @Id
  Integer id;

  String something;

  @ManyToOne
//	@JoinColumns({
//			@JoinColumn(name="parent_one_key", referencedColumnName="one_key"),
//			@JoinColumn(name="parent_two_key", referencedColumnName="two_key")
//	})
    CKeyParent parent;

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

  public CKeyParent getParent() {
    return parent;
  }

  public void setParent(CKeyParent parent) {
    this.parent = parent;
  }


}
