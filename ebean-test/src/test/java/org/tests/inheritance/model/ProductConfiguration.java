package org.tests.inheritance.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@DiscriminatorValue("1")
public class ProductConfiguration extends Configuration {
  private String productName;

  @OneToMany(mappedBy = "productConfiguration")
  private List<CalculationResult> results;

  public ProductConfiguration() {
    super();
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public List<CalculationResult> getResults() {
    return results;
  }

  public void setResults(List<CalculationResult> results) {
    this.results = results;
  }
}
