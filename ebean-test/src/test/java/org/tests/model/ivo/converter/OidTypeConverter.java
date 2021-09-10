package org.tests.model.ivo.converter;

import io.ebean.config.ScalarTypeConverter;
import org.tests.model.ivo.Oid;

public class OidTypeConverter implements ScalarTypeConverter<Oid<?>,Long> {

  public static final Oid<?> NULL_VALUE = new Oid<>(0);

  @Override
  public Oid<?> getNullValue() {
    return NULL_VALUE;
  }

  @Override
  public Oid<?> wrapValue(Long scalarType) {
    return new Oid<>(scalarType);
  }

  @Override
  public Long unwrapValue(Oid<?> beanType) {
    if (NULL_VALUE.equals(beanType)) {
      return null;
    }
    return beanType.getValue();
  }

}
