package io.ebean.plugin;

import java.util.List;
import java.util.Set;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.databind.ScalarType;
import io.ebean.dbmigration.DbMigrationInfo;
import io.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import io.ebeanservice.docstore.api.support.DocStructure;

/**
 * Property of a entity bean that can be read.
 */
public interface Property {

  /**
   * Return the name of the property.
   */
  String getName();

  /**
   * Return the value of the property on the given bean.
   */
  Object getVal(Object bean);

  /**
   * Return true if this is a OneToMany or ManyToMany property.
   */
  boolean isMany();

  String getDbColumn();

  String getDbColumnDefault();

  String getDbComment();

  int getDbEncryptedType();

  boolean isDbEncrypted();

  boolean isLocalEncrypted();

  ScalarType<Object> getScalarType();

  int getDbType(boolean platformTypes);

  String getFullBeanName();

  String renderDbType(DbPlatformType dbType, boolean strict);

  List<DbMigrationInfo> getDbMigrationInfos();

  Set<String> getDbCheckConstraintValues();

  boolean isSecondaryTable();

  boolean isDraftOnly();

  boolean isExcludedFromHistory();

  boolean isId();

  boolean isNullable();

  boolean isDDLNotNull();

  boolean isUnique();

  boolean isUseIdGenerator();

  boolean isDDLColumn();
  
  void docStoreMapping(DocMappingBuilder mapping, String prefix);

  void docStoreInclude(boolean includeByDefault, DocStructure docStructure);
  
  BeanType<?> getBeanType();

  int getPropertyIndex();
}
