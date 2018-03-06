package misc.migration.v1_0;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable
public class CKeyParentId {

  Integer oneKey;

  @Size(max=127)
  String twoKey;

  public CKeyParentId() {

  }

  public CKeyParentId(Integer oneKey, String twoKey) {
    this.oneKey = oneKey;
    this.twoKey = twoKey;
  }

  public Integer getOneKey() {
    return oneKey;
  }

  public void setOneKey(Integer oneKey) {
    this.oneKey = oneKey;
  }

  public String getTwoKey() {
    return twoKey;
  }

  public void setTwoKey(String twoKey) {
    this.twoKey = twoKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CKeyParentId)) {
      return false;
    }

    CKeyParentId otherKey = (CKeyParentId) o;
    return otherKey.hashCode() == hashCode();
  }

  @Override
  public int hashCode() {
    int hc = getClass().getName().hashCode();
    hc = 31 * hc + oneKey;
    hc = 31 * hc + twoKey.hashCode();
    return hc;
  }
}
