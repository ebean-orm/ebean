package org.tests.query.aggregation;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Formula2;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * StockProduct entity demonstrating both @Formula (simple, no join) and
 * @Formula2 (logical path expression with auto-join).
 */
@Entity
public class AggStockProduct {

  @Id
  private Integer id;

  @ManyToOne
  private AggProduct product;

  private String stock;
  private int quantity;
  private int quantityReserved;

  /**
   * Simple computed column — no join needed.
   * Using @Formula with ${ta} prefix for correctness when joins are present.
   */
  @Formula(select = "${ta}.quantity - ${ta}.quantity_reserved")
  private int availableQuantity;

  /**
   * Computed value requiring a join to the product table.
   * @Formula2 uses the logical path "product.price" and auto-adds the join.
   */
  @Formula2("quantity * product.price")
  private int totalPrice;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AggProduct getProduct() {
    return product;
  }

  public void setProduct(AggProduct product) {
    this.product = product;
  }

  public String getStock() {
    return stock;
  }

  public void setStock(String stock) {
    this.stock = stock;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public int getQuantityReserved() {
    return quantityReserved;
  }

  public void setQuantityReserved(int quantityReserved) {
    this.quantityReserved = quantityReserved;
  }

  public int getAvailableQuantity() {
    return availableQuantity;
  }

  public int getTotalPrice() {
    return totalPrice;
  }
}
