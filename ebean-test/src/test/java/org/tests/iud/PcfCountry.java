package org.tests.iud;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PcfCountry extends PcfModel {


  @OneToMany(cascade = CascadeType.ALL)
  private List<PcfCity> cities = new ArrayList<>();

  public void addCity(PcfCity city) {
    cities.add(city);
  }

}
