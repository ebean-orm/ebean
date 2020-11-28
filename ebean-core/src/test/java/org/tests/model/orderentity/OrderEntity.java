package org.tests.model.orderentity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "s_orders")
public class OrderEntity {

  /**
   * Rob Note: Ideally this would be a UUID rather than a String type - then Ebean would automatically
   * assign a UUID based id generator and 'do the right thing'.
   */
  @Id
  @Column(name = "uuid")
  @Size(max=40)
  private String id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
  private List<OrderItemEntity> items;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<OrderItemEntity> getItems() {
    return items;
  }

  public void setItems(List<OrderItemEntity> items) {
    this.items = items;
  }


}
