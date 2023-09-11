package org.example.otherpackage

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class GenericTypeAttributeConverter : AttributeConverter<GenericType<GenericTypeArgument>, String> {
  override fun convertToDatabaseColumn(attribute: GenericType<GenericTypeArgument>): String {
    return attribute.data.name
  }

  override fun convertToEntityAttribute(dbData: String): GenericType<GenericTypeArgument> {
    return GenericType(GenericTypeArgument.valueOf(dbData))
  }
}
