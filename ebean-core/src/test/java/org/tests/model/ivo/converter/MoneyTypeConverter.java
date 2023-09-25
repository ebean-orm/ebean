package org.tests.model.ivo.converter;

import org.tests.model.ivo.Money;

import jakarta.persistence.AttributeConverter;
import java.math.BigDecimal;

/**
 * Converts between Money and BigDecimal.
 */
public class MoneyTypeConverter implements AttributeConverter<Money,BigDecimal> {

  @Override
  public BigDecimal convertToDatabaseColumn(Money beanType) {
    return beanType.getAmount();
  }

  @Override
  public Money convertToEntityAttribute(BigDecimal scalarType) {
    return new Money(scalarType);
  }

}
