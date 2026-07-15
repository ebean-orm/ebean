package io.ebean.querybean.generator;

/**
 * Metadata for a single {@code @DtoConvert(value = ConverterType.class, method = "name")}
 * property conversion.
 * <p>
 * Dispatch strategy is decided purely by whether the referenced method is {@code static}:
 * <ul>
 *   <li>{@code static} - the generated mapper emits a direct static call
 *   ({@code ConverterType.method(source.getX())}), no registration/constructor wiring needed at
 *   all - for common, reusable, dependency-free conversions.</li>
 *   <li>instance - the generated mapper resolves one shared instance via
 *   {@code DtoConverterManager.get(ConverterType.class)}, wired as a constructor
 *   parameter/field (same shape as nested-mapper constructor injection) - for conversions
 *   needing a real dependency.</li>
 * </ul>
 */
final class DtoConverterMeta {

  private final String typeFullName;
  private final String methodName;
  private final boolean isStatic;

  DtoConverterMeta(String typeFullName, String methodName, boolean isStatic) {
    this.typeFullName = typeFullName;
    this.methodName = methodName;
    this.isStatic = isStatic;
  }

  String typeFullName() {
    return typeFullName;
  }

  String typeShortName() {
    return Split.shortName(typeFullName);
  }

  String methodName() {
    return methodName;
  }

  boolean isStatic() {
    return isStatic;
  }

  /**
   * Field/parameter name used to hold the resolved instance for instance-dispatch conversions -
   * derived from the converter type's short name, lower-camel-cased.
   */
  String fieldName() {
    String name = typeShortName();
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DtoConverterMeta)) {
      return false;
    }
    return typeFullName.equals(((DtoConverterMeta) o).typeFullName);
  }

  @Override
  public int hashCode() {
    return typeFullName.hashCode();
  }
}
