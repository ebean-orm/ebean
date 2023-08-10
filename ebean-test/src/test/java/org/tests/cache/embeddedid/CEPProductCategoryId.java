package org.tests.cache.embeddedid;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CEPProductCategoryId {

  // primitive id values as part of an EmbeddedId (NPE #1722)
  private long categoryId;
  private long productId;

  public long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(long categoryId) {
    this.categoryId = categoryId;
  }

  public long getProductId() {
    return productId;
  }

  public void setProductId(long productId) {
    this.productId = productId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CEPProductCategoryId that = (CEPProductCategoryId) o;
    return categoryId == that.categoryId &&
      productId == that.productId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(categoryId, productId);
  }
}
