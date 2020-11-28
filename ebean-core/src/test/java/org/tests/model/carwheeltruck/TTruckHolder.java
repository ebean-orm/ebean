package org.tests.model.carwheeltruck;

import org.tests.model.basic.EBasic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class TTruckHolder {

  @Id
  private long id;

  @Version
  private long version;

  private final String name;

  /**
   * Inheritance but at BOTTOM layer (so no discriminator needed).
   */
  @ManyToOne(optional = false)
  private TTruck truck;

  @ManyToOne
  private EBasic basic;

  @OneToMany(cascade = ALL, mappedBy = "owner")
  private List<TTruckHolderItem> items;

  public TTruckHolder(String name, TTruck truck) {
    this.name = name;
    this.truck = truck;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public TTruck getTruck() {
    return truck;
  }

  public void setTruck(TTruck truck) {
    this.truck = truck;
  }

  public EBasic getBasic() {
    return basic;
  }

  public void setBasic(EBasic basic) {
    this.basic = basic;
  }

  public List<TTruckHolderItem> getItems() {
    return items;
  }

  public void setItems(List<TTruckHolderItem> items) {
    this.items = items;
  }
}
