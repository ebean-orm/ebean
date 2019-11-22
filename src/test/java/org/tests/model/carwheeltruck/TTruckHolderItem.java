package org.tests.model.carwheeltruck;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class TTruckHolderItem {

  @Id
  private Long id;

  private final UUID someUid;

  private final String foo;

  @ManyToOne(optional = false)
  private TTruckHolder owner;

  public TTruckHolderItem(String foo) {
    this.someUid = UUID.randomUUID();
    this.foo = foo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UUID getSomeUid() {
    return someUid;
  }

  public String getFoo() {
    return foo;
  }

  public TTruckHolder getOwner() {
    return owner;
  }

  public void setOwner(TTruckHolder owner) {
    this.owner = owner;
  }
}
