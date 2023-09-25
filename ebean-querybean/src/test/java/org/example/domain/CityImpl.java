package org.example.domain;

import org.example.domain.api.ACity;
import org.example.domain.api.ACountry;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class CityImpl implements ACity {

  @Id
  long id;

  String name;

  @ManyToOne(targetEntity = CountryImpl.class)
  ACountry country;

  @Override
  public long id() {
    return id;
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public ACountry country() {
    return country;
  }

}
