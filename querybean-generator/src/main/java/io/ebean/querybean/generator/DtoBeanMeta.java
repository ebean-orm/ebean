package io.ebean.querybean.generator;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata for one {@code @DtoMapping(source, target)} pair - drives generation of a single
 * {@code XxxMapper} class implementing {@code io.ebean.DtoMapper<source, target>}.
 */
class DtoBeanMeta {

  private final TypeElement source;
  private final TypeElement target;
  private final String mapperPackage;
  private final String mapperShortName;
  private final String mapperFullName;
  private final List<DtoPropertyMeta> properties = new ArrayList<>();
  private final List<DtoMapperVariant> variants = new ArrayList<>();
  private DtoBuilderMeta builderMeta;
  private DtoSetterMeta setterMeta;

  /** Cycle detection state - see {@link DtoMappingReader#checkForCycles()}. */
  enum Visit { WHITE, GRAY, BLACK }
  Visit visit = Visit.WHITE;

  /**
   * Set true if this type is used as a {@code NESTED_ONE}/{@code NESTED_MANY} property by any
   * other {@code @DtoMapping} pair - see {@link DtoMappingReader#markNestedElsewhere()}. Drives
   * whether {@link DtoMapperWriter} needs to route this mapper's own construction through the
   * {@code DtoMapContext} identity cache (only ever useful when the same source instance can be
   * reached via more than one path in the graph).
   */
  private boolean nestedElsewhere;

  DtoBeanMeta(TypeElement source, TypeElement target, String mapperPackage, String mapperName) {
    this.source = source;
    this.target = target;
    this.mapperPackage = mapperPackage;
    this.mapperShortName = mapperName != null && !mapperName.isEmpty()
      ? mapperName
      : target.getSimpleName() + "Mapper";
    this.mapperFullName = mapperPackage + "." + mapperShortName;
  }

  TypeElement source() {
    return source;
  }

  TypeElement target() {
    return target;
  }

  String mapperPackage() {
    return mapperPackage;
  }

  String mapperShortName() {
    return mapperShortName;
  }

  String mapperFullName() {
    return mapperFullName;
  }

  String sourceFullName() {
    return source.getQualifiedName().toString();
  }

  String targetFullName() {
    return target.getQualifiedName().toString();
  }

  List<DtoPropertyMeta> properties() {
    return properties;
  }

  void addProperty(DtoPropertyMeta property) {
    properties.add(property);
  }

  List<DtoMapperVariant> variants() {
    return variants;
  }

  void addVariant(DtoMapperVariant variant) {
    variants.add(variant);
  }

  /**
   * The detected+selected builder-based construction path for this target, or {@code null} if
   * the mapper should construct the target via a plain positional constructor call instead - see
   * {@link DtoMappingReader#resolveBuilder(DtoBeanMeta, String)}.
   */
  DtoBuilderMeta builderMeta() {
    return builderMeta;
  }

  void builderMeta(DtoBuilderMeta builderMeta) {
    this.builderMeta = builderMeta;
  }

  /**
   * The detected+selected setter-based construction path for this target, or {@code null} if not
   * applicable - see {@link DtoMappingReader#resolveSetter(DtoBeanMeta, String)}. Mutually
   * exclusive with {@link #builderMeta()} - a builder, when selected, always takes priority (see
   * {@link DtoMappingReader#resolveAndValidate()}).
   */
  DtoSetterMeta setterMeta() {
    return setterMeta;
  }

  void setterMeta(DtoSetterMeta setterMeta) {
    this.setterMeta = setterMeta;
  }

  /**
   * Return the distinct instance-dispatch {@code @DtoConvert} converters used by this mapper's
   * properties (static-dispatch converters need no field/constructor wiring, so are excluded),
   * in first-seen order, deduplicated by converter type - so a converter type used by more than
   * one property still results in a single shared constructor parameter/field.
   */
  List<DtoConverterMeta> converterDeps() {
    List<DtoConverterMeta> deps = new ArrayList<>();
    for (DtoPropertyMeta property : properties) {
      DtoConverterMeta converter = property.converter();
      if (converter != null && !converter.isStatic() && !deps.contains(converter)) {
        deps.add(converter);
      }
    }
    return deps;
  }

  /**
   * Return true if this type is nested ({@code NESTED_ONE}/{@code NESTED_MANY}) under some other
   * DTO - i.e. the same source instance could be reached via more than one path in the graph, so
   * identity de-duplication via {@code DtoMapContext} can actually matter for it.
   */
  boolean nestedElsewhere() {
    return nestedElsewhere;
  }

  void markNestedElsewhere() {
    this.nestedElsewhere = true;
  }

  @Override
  public String toString() {
    return sourceFullName() + " -> " + targetFullName();
  }
}
