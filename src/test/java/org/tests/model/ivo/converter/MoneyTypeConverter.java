package org.tests.model.ivo.converter;

import org.tests.model.ivo.Money;

import javax.persistence.AttributeConverter;
import java.math.BigDecimal;

/**
 * Converts between Money and BigDecimal.
 */
public class MoneyTypeConverter implements AttributeConverter<Money,BigDecimal> {

  public BigDecimal convertToDatabaseColumn(Money beanType) {
    return beanType.getAmount();
  }

  public Money convertToEntityAttribute(BigDecimal scalarType) {
    return new Money(scalarType);
  }

}
