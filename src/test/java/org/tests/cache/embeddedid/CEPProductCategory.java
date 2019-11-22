package org.tests.cache.embeddedid;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

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
