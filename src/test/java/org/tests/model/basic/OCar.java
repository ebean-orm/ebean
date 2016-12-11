package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class OCar extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private String vin;

  private String name;

  @OneToOne(mappedBy = "car", cascade = CascadeType.ALL)
  private OGearBox gearBox;

  @OneToOne(mappedBy = "car", cascade = CascadeType.ALL)
  private OEngine engine;

  public OCar() {
  }

  public OCar(String vin, String name) {
    super();
    this.vin = vin;
    this.name = name;
  }

  public String getVin() {
    return vin;
  }

  public void setVin(String vin) {
    this.vin = vin;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OGearBox getGearBox() {
    return gearBox;
  }

  public void setGearBox(OGearBox gearBox) {
    this.gearBox = gearBox;
    gearBox.setCar(this);
  }

  public OEngine getEngine() {
    return engine;
  }

  public void setEngine(OEngine engine) {
    this.engine = engine;
    engine.setCar(this);
  }
}
