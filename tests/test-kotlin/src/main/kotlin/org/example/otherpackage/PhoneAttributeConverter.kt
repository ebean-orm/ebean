package org.example.otherpackage

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PhoneAttributeConverter : AttributeConverter<PhoneNumber, String> {
  override fun convertToDatabaseColumn(attribute: PhoneNumber): String {
    return attribute.msisdn
  }

  override fun convertToEntityAttribute(dbData: String): PhoneNumber {
    return PhoneNumber(dbData)
  }
}
