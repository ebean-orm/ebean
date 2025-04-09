package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.BeanCascadeInfo;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for reading deployment annotations.
 */
public abstract class AnnotationParser extends AnnotationBase {

  final DeployBeanInfo<?> info;
  final DeployBeanDescriptor<?> descriptor;
  final Class<?> beanType;
  final ReadAnnotationConfig readConfig;

  AnnotationParser(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info.getUtil());
    this.readConfig = readConfig;
    this.info = info;
    this.beanType = info.getDescriptor().getBeanType();
    this.descriptor = info.getDescriptor();
  }

  /**
   * read the deployment annotations.
   */
  @Override
  public abstract void parse();

  /**
   * Read the Id annotation on an embeddedId.
   */
  void readIdAssocOne(DeployBeanPropertyAssoc<?> prop) {
    prop.setNullable(false);
    if (prop.isIdClass()) {
      prop.setImportedPrimaryKey();
    } else {
      prop.setId();
      prop.setEmbedded();
      info.setEmbeddedId(prop);
    }
  }

  /**
   * Read the Id annotation on scalar property.
   */
  void readIdScalar(DeployBeanProperty prop) {
    prop.setNullable(false);
    if (prop.isIdClass()) {
      prop.setImportedPrimaryKey();
    } else {
      prop.setId();
      if (prop.getPropertyType().equals(UUID.class) && readConfig.isIdGeneratorAutomatic()) {
        descriptor.setUuidGenerator();
      }
    }
  }

  /**
   * Helper method to set cascade types to the CascadeInfo on BeanProperty.
   */
  void setCascadeTypes(CascadeType[] cascadeTypes, BeanCascadeInfo cascadeInfo) {
    if (cascadeTypes != null && cascadeTypes.length > 0) {
      cascadeInfo.setTypes(cascadeTypes);
    }
  }

  /**
   * Read an AttributeOverrides if they exist for this embedded bean.
   */
  void readEmbeddedAttributeOverrides(DeployBeanPropertyAssocOne<?> prop) {
    Set<AttributeOverride> attrOverrides = annotationAttributeOverrides(prop);
    if (!attrOverrides.isEmpty()) {
      Map<String, Column> propMap = new HashMap<>(attrOverrides.size());
      for (AttributeOverride attrOverride : attrOverrides) {
        propMap.put(attrOverride.name(), attrOverride.column());
      }
      prop.getDeployEmbedded().putAll(propMap);
    }
  }

  void readColumn(Column columnAnn, DeployBeanProperty prop) {
    setColumnName(prop, columnAnn.name());
    prop.setDbInsertable(columnAnn.insertable());
    prop.setDbUpdateable(columnAnn.updatable());
    prop.setNullable(columnAnn.nullable());
    prop.setUnique(columnAnn.unique());
    if (columnAnn.precision() > 0) {
      prop.setDbLength(columnAnn.precision());
    } else if (columnAnn.length() != 0) {
      // set default 255 on DbTypeMap
      prop.setDbLength(columnAnn.length());
    }
    prop.setDbScale(columnAnn.scale());
    prop.setDbColumnDefn(columnAnn.columnDefinition());

    String baseTable = descriptor.getBaseTable();
    String tableName = columnAnn.table();
    if (!"".equals(tableName) && !tableName.equalsIgnoreCase(baseTable)) {
      // its on a secondary table...
      prop.setSecondaryTable(tableName);
    }
  }

  protected void setColumnName(DeployBeanProperty prop, String name) {
    if (!isEmpty(name)) {
      prop.setDbColumn(databasePlatform.convertQuotedIdentifiers(name));
    }
  }

  String[] convertColumnNames(String[] columnNames) {
    for (int i = 0; i < columnNames.length; i++) {
      columnNames[i] = databasePlatform.convertQuotedIdentifiers(columnNames[i]);
    }
    return columnNames;
  }

  /**
   * Process any formula from &#64;Formula or &#64;Where.
   */
  protected String processFormula(String source) {
    return source == null ? null : source.replace("${dbTableName}", descriptor.getBaseTable());
  }
}
