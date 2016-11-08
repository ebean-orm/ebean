package com.avaje.tests.model.ivo.converter;

import com.avaje.tests.model.ivo.Oid;

public class OidTypeConverter {//implements ScalarTypeConverter<Oid<?>,Long> {

  public Oid<?> wrapValue(Long scalarType) {
    if (scalarType == null) {
      return null;
    }
    return new Oid<>(scalarType);
  }

  public Long unwrapValue(Oid<?> beanType) {
    if (beanType == null) {
      return null;
    }
    return beanType.getValue();
  }

}
