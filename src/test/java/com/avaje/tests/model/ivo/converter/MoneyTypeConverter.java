package com.avaje.tests.model.ivo.converter;

import com.avaje.tests.model.ivo.Money;

import java.math.BigDecimal;

/**
 * Converts between Money and BigDecimal.
 */
public class MoneyTypeConverter {//implements ScalarTypeConverter<Money,BigDecimal>{

  public BigDecimal unwrapValue(Money beanType) {
    return beanType.getAmount();
  }

  public Money wrapValue(BigDecimal scalarType) {
    return new Money(scalarType);
  }

}
