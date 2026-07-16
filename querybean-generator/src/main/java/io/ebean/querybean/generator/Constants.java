package io.ebean.querybean.generator;

interface Constants {

  String AT_GENERATED = "@io.ebean.typequery.Generated(\"io.ebean.querybean.generator\")";
  String AT_TYPEQUERYBEAN = "@io.ebean.typequery.TypeQueryBean(\"v1\")";
  String TYPEQUERYBEAN = "io.ebean.typequery.TypeQueryBean";
  String GENERATED = "io.ebean.typequery.Generated";

  String ONE_TO_MANY = "jakarta.persistence.OneToMany";
  String MANY_TO_MANY = "jakarta.persistence.ManyToMany";
  String MAPPED_SUPERCLASS = "jakarta.persistence.MappedSuperclass";
  String DISCRIMINATOR_VALUE = "jakarta.persistence.DiscriminatorValue";
  String INHERITANCE = "jakarta.persistence.Inheritance";
  String ENTITY = "jakarta.persistence.Entity";
  String EMBEDDABLE = "jakarta.persistence.Embeddable";
  String CONVERTER = "jakarta.persistence.Converter";
  String EBEAN_COMPONENT = "io.ebean.annotation.EbeanComponent";

  String DBARRAY = "io.ebean.annotation.DbArray";
  String DBJSON = "io.ebean.annotation.DbJson";
  String DBJSONB = "io.ebean.annotation.DbJsonB";
  String DBNAME = "io.ebean.annotation.DbName";

  String MODULEINFO = "io.ebean.config.ModuleInfo";
  String METAINF_MANIFEST = "META-INF/ebean-generated-info.mf";
  String METAINF_SERVICES_MODULELOADER = "META-INF/services/io.ebean.config.EntityClassRegister";

  String DTO_MAPPING = "io.ebean.annotation.DtoMapping";
  // DtoMapping is @Repeatable - when used more than once on the same element javac wraps
  // the repeated instances in this synthetic container annotation instead, so it must also
  // be registered as a supported annotation type or the processor is never invoked for it.
  String DTO_MAPPING_LIST = "io.ebean.annotation.DtoMapping.List";
  String DTO_PATH = "io.ebean.annotation.DtoPath";
  String DTO_REF = "io.ebean.annotation.DtoRef";
  String DTO_CONVERT = "io.ebean.annotation.DtoConvert";
  String DTO_CONVERTERS = "io.ebean.annotation.DtoConverters";
  String DTO_MIXIN = "io.ebean.annotation.DtoMixin";
  String DTO_CONVERTER_MANAGER = "io.ebean.DtoConverterManager";
  String METAINF_SERVICES_DTOMAPPERREGISTER = "META-INF/services/io.ebean.config.DtoMapperRegister";

  String AVAJE_LANG_NULLABLE = "org.jspecify.annotations.Nullable";
  String JAVA_COLLECTION = "java.util.Collection";
  String EXPRESSIONLIST = "io.ebean.ExpressionList";
  String EXPR = "io.ebean.Expr";
  String CONSUMER = "java.util.function.Consumer";
}
