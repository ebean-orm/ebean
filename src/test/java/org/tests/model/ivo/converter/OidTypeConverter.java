package org.tests.model.ivo.converter;

import io.ebean.config.ScalarTypeConverter;
import org.tests.model.ivo.Oid;

public class OidTypeConverter implements ScalarTypeConverter<Oid<?>,Long> {

  @Override
  public Oid<?> getNullValue() {
    return null;
  }

  @Override
  public Oid<?> wrapValue(Long scalarType) {
    if (scalarType == null) {
      return null;
    }
    return new Oid<>(scalarType);
  }

  @Override
  public Long unwrapValue(Oid<?> beanType) {
    if (beanType == null) {
      return null;
    }
    return beanType.getValue();
  }

}
