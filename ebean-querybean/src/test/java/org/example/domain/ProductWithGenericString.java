package org.example.domain;

import org.example.domain.ProductWithGenericLong;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Product entity bean.
 */
@Entity
@Table(name = "o_product", schema = "foo")
public class ProductWithGenericString extends GenericBaseModel<String> {

  @Size(max = 20)
  String sku;

  String name;

  /**
   * Return sku.
   */
  public String getSku() {
    return sku;
  }

  /**
   * Set sku.
   */
  public void setSku(String sku) {
    this.sku = sku;
  }

  /**
   * Return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   */
  public void setName(String name) {
    this.name = name;
  }

}
