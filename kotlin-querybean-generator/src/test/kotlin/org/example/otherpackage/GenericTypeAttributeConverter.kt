package org.example.otherpackage

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class GenericTypeAttributeConverter : AttributeConverter<GenericType<GenericTypeArgument>, String> {
  override fun convertToDatabaseColumn(attribute: GenericType<GenericTypeArgument>): String {
    return attribute.data.name
  }

  override fun convertToEntityAttribute(dbData: String): GenericType<GenericTypeArgument> {
    return GenericType(GenericTypeArgument.valueOf(dbData))
  }
}
