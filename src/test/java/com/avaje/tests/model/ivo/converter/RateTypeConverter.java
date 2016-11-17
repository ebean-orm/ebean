package com.avaje.tests.model.ivo.converter;

import com.avaje.tests.model.ivo.Rate;

import java.math.BigDecimal;

/**
 * Converts between Rate and BigDecimal.
 */
public class RateTypeConverter {//implements ScalarTypeConverter<Rate,BigDecimal>{

  public BigDecimal unwrapValue(Rate beanType) {
    return beanType.getValue();
  }

  public Rate wrapValue(BigDecimal scalarType) {
    return new Rate(scalarType);
  }

}
