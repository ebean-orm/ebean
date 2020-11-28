package org.tests.model.docstore;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;

import org.tests.model.basic.Product;

import io.ebean.annotation.DocStore;

@DocStore
@DiscriminatorValue("PR")
public class ProductReport extends Report {

  @ManyToOne
  private Product product;
  
  public Product getProduct() {
    return product;
  }
  public void setProduct(Product product) {
    this.product = product;
  }
}
