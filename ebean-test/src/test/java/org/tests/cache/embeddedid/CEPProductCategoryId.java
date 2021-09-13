package org.tests.cache.embeddedid;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CEPProductCategoryId {

  // primitive id values as part of an EmbeddedId (NPE #1722)
  private long customerId;
  private long addressId;

  public long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(long customerId) {
    this.customerId = customerId;
  }

  public long getAddressId() {
    return addressId;
  }

  public void setAddressId(long addressId) {
    this.addressId = addressId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CEPProductCategoryId that = (CEPProductCategoryId) o;
    return customerId == that.customerId &&
      addressId == that.addressId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, addressId);
  }
}
