package org.example.otherpackage

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ValidEmailAttributeConverter : AttributeConverter<ValidEmail, String> {
  override fun convertToDatabaseColumn(attribute: ValidEmail): String {
    return attribute.emailAddress
  }

  override fun convertToEntityAttribute(dbData: String): ValidEmail {
    return ValidEmail(dbData)
  }
}
