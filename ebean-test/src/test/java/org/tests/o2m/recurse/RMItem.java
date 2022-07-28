package org.tests.o2m.recurse;

import javax.persistence.*;
import java.util.List;

@Entity
public class RMItem {

  @Id
  private long itemId;

  @ManyToOne
  @JoinColumn(name = "item_group_id")
  private RMItem itemGroup;

  private String name;

  @OneToMany(mappedBy = "itemGroup")
  private List<RMItem> subItems;

  public RMItem() {
  }

  public RMItem(String name) {
    this.name = name;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  public RMItem getItemGroup() {
    return itemGroup;
  }

  public void setItemGroup(RMItem itemGroup) {
    this.itemGroup = itemGroup;
  }

  public List<RMItem> getSubItems() {
    return subItems;
  }

  public void setSubItems(List<RMItem> subItems) {
    this.subItems = subItems;
  }
}
