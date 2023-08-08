package org.example.otherpackage

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class PhoneAttributeConverter : AttributeConverter<PhoneNumber, String> {
  override fun convertToDatabaseColumn(attribute: PhoneNumber): String {
    return attribute.msisdn
  }

  override fun convertToEntityAttribute(dbData: String): PhoneNumber {
    return PhoneNumber(dbData)
  }
}
