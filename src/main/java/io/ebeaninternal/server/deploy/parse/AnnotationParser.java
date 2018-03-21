package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.BeanCascadeInfo;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.validation.groups.Default;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for reading deployment annotations.
 */
public abstract class AnnotationParser extends AnnotationBase {

  protected final DeployBeanInfo<?> info;

  protected final DeployBeanDescriptor<?> descriptor;

  protected final Class<?> beanType;

  protected final boolean validationAnnotations;

  protected final ReadAnnotationConfig readConfig;

  public AnnotationParser(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info.getUtil());
    this.readConfig = readConfig;
    this.validationAnnotations = readConfig.isJavaxValidationAnnotations();
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
  protected void readIdAssocOne(DeployBeanPropertyAssoc<?> prop) {
    prop.setNullable(false);
    if (prop.isIdClass()) {
      prop.setImportedPrimaryKey();
    } else {
      prop.setId();
      prop.setEmbedded();
    }
  }

  /**
   * Read the Id annotation on scalar property.
   */
  protected void readIdScalar(DeployBeanProperty prop) {
    prop.setNullable(false);
    if (prop.isIdClass()) {
      prop.setImportedPrimaryKey();
    } else {
      prop.setId();
      if (prop.getPropertyType().equals(UUID.class)) {
        if (descriptor.getIdGeneratorName() == null) {
          descriptor.setUuidGenerator();
        }
      }
    }
  }

  /**
   * Helper method to set cascade types to the CascadeInfo on BeanProperty.
   */
  protected void setCascadeTypes(CascadeType[] cascadeTypes, BeanCascadeInfo cascadeInfo) {
    if (cascadeTypes != null && cascadeTypes.length > 0) {
      cascadeInfo.setTypes(cascadeTypes);
    }
  }

  /**
   * Read an AttributeOverrides if they exist for this embedded bean.
   */
  protected void readEmbeddedAttributeOverrides(DeployBeanPropertyAssocOne<?> prop) {

    Set<AttributeOverride> attrOverrides = getAll(prop, AttributeOverride.class);
    if (!attrOverrides.isEmpty()) {
      HashMap<String, String> propMap = new HashMap<>(attrOverrides.size());
      for (AttributeOverride attrOverride : attrOverrides) {
        propMap.put(attrOverride.name(), attrOverride.column().name());
      }
      prop.getDeployEmbedded().putAll(propMap);
    }

  }

  protected void readColumn(Column columnAnn, DeployBeanProperty prop) {

    if (!isEmpty(columnAnn.name())) {
      String dbColumn = databasePlatform.convertQuotedIdentifiers(columnAnn.name());
      prop.setDbColumn(dbColumn);
    }

    prop.setDbInsertable(columnAnn.insertable());
    prop.setDbUpdateable(columnAnn.updatable());
    prop.setNullable(columnAnn.nullable());
    prop.setUnique(columnAnn.unique());
    if (columnAnn.precision() > 0) {
      prop.setDbLength(columnAnn.precision());
    } else if (columnAnn.length() != 255) {
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

  /**
   * Return true if the validation groups are {@link Default} (respectively empty)
   * can be applied to DDL generation.
   */
  protected boolean isEbeanValidationGroups(Class<?>[] groups) {
    if (!util.isUseJavaxValidationNotNull()) {
      return false;
    }
    if (groups.length == 0
      || groups.length == 1 && javax.validation.groups.Default.class.isAssignableFrom(groups[0])) {
      return true;
    }
    return false;
  }
}
