package org.tests.iud;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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
