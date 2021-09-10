package org.tests.singleTableInheritance.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "warehouses")
public class Warehouse {
  @Id
  @Column(name = "id")
  private Integer id;

  @ManyToOne//(optional = false) //todo: should this be nullable with assertions made?
  @JoinColumn(name = "officezoneid")
  private ZoneInternal officeZone;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = "warehousesshippingzones",
    joinColumns = {@JoinColumn(name = "warehouseid", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "shippingzoneid", referencedColumnName = "id")}
  )
  private Set<ZoneExternal> shippingZones;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ZoneInternal getOfficeZone() {
    return officeZone;
  }

  public void setOfficeZone(ZoneInternal officeZone) {
    this.officeZone = officeZone;
  }

  public Set<ZoneExternal> getShippingZones() {
    return shippingZones;
  }

  public void setShippingZones(Set<ZoneExternal> shippingZones) {
    this.shippingZones = shippingZones;
  }

}
