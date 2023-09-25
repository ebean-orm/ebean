package org.example.domain;

import org.example.domain.api.ACountry;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CountryImpl implements ACountry {

  @Id
  String code;

  @Override
  public String code() {
    return code;
  }
}
