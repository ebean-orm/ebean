package com.avaje.tests.model.ivo.converter;

import com.avaje.tests.model.ivo.Rate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;

/**
 * Converts between Rate and BigDecimal.
 */
@Converter
public class RateTypeConverter implements AttributeConverter<Rate,BigDecimal> {

  @Override
  public BigDecimal convertToDatabaseColumn(Rate attribute) {
    return attribute.getValue();
  }

  @Override
  public Rate convertToEntityAttribute(BigDecimal dbData) {
    return new Rate(dbData);
  }
}
