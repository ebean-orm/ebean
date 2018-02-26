package io.ebeaninternal.server.type;

public class ArrayElementConverterEnum implements ArrayElementConverter<String> {

  final ScalarType<?> scalarType;

  final Class<? extends Enum<?>> valueType1;

  public ArrayElementConverterEnum(ScalarType<?> scalarType, Class<? extends Enum<?>> valueType1) {
    this.scalarType = scalarType;
    this.valueType1 = valueType1;
  }

  @Override
  public String toElement(Object rawValue) {
    // FIXME: Don't understand for what is this.
    Enum<?>[] enumConstants = valueType1.getEnumConstants();
    if (scalarType == null) {
      return rawValue.toString();
    }
    return scalarType.format(rawValue);
  }
}
