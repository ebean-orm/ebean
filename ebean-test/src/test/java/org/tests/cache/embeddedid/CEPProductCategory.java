package org.tests.cache.embeddedid;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;

@IdClass(CEPProductCategoryId.class)
@Entity
public class CEPProductCategory {

  @Id
  @ManyToOne
  private CEPCategory category;

  @Id
  @ManyToOne
  private CEPProduct product;

  public CEPProductCategory(CEPCategory category, CEPProduct product) {
    this.category = category;
    this.product = product;
  }

  public CEPCategory getCategory() {
    return category;
  }

  public void setCategory(CEPCategory category) {
    this.category = category;
  }

  public CEPProduct getProduct() {
    return product;
  }

  public void setProduct(CEPProduct product) {
    this.product = product;
  }
}
