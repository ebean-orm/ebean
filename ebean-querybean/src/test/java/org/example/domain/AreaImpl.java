package org.example.domain;

import org.example.domain.api.ACity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class AreaImpl {

  @Id
  long id;

  @OneToMany(targetEntity = CityImpl.class, cascade = CascadeType.ALL)
  List<ACity> cities;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<ACity> getCities() {
    return cities;
  }

  public void setCities(List<ACity> cities) {
    this.cities = cities;
  }
}
