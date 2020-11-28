package org.tests.model.orderentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "s_order_items")
public class OrderItemEntity {

  /**
   * Rob Note: Ideally this would be a UUID rather than a String type - then Ebean would automatically
   * assign a UUID based id generator and 'do the right thing'.
   */
  @Id
  @Column(name = "uuid")
  @Size(max=40)
  private String id;

  @Column(name = "product_variant_uuid")
  private String variantId;

  @JoinColumn(name = "order_uuid")
  @ManyToOne
  private OrderEntity order;

  private int quantity;

  private BigDecimal amount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVariantId() {
    return variantId;
  }

  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }

  public OrderEntity getOrder() {
    return order;
  }

  public void setOrder(OrderEntity order) {
    this.order = order;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

}
