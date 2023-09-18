package io.ebean.querybean.generator;

interface Constants {

  String AT_GENERATED = "@Generated(\"io.ebean.querybean.generator\")";

  String AT_TYPEQUERYBEAN = "@TypeQueryBean(\"v1\")";
  String GENERATED = "io.ebean.typequery.Generated";

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

  String TQROOTBEAN = "io.ebean.typequery.TQRootBean";
  String TQASSOC = "io.ebean.typequery.TQAssoc";
  String TQASSOCBEAN = "io.ebean.typequery.TQAssocBean";
  String TQPROPERTY = "io.ebean.typequery.TQProperty";
  String TYPEQUERYBEAN = "io.ebean.typequery.TypeQueryBean";
  String DATABASE = "io.ebean.Database";
  String DB = "io.ebean.DB";
  String FETCHGROUP = "io.ebean.FetchGroup";
  String QUERY = "io.ebean.Query";
  String TRANSACTION = "io.ebean.Transaction";

  String MODULEINFO = "io.ebean.config.ModuleInfo";
  String METAINF_MANIFEST = "META-INF/ebean-generated-info.mf";
  String METAINF_SERVICES_MODULELOADER = "META-INF/services/io.ebean.config.EntityClassRegister";

  String AVAJE_LANG_NULLABLE = "io.avaje.lang.Nullable";
  String JAVA_COLLECTION = "java.util.Collection";
}
