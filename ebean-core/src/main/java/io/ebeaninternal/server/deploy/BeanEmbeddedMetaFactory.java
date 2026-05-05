package io.ebeaninternal.server.deploy;

import io.ebean.config.BeanNotRegisteredException;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

import jakarta.persistence.Column;
import java.util.Map;

/**
 * Creates BeanProperties for Embedded beans that have deployment information
 * such as the actual DB column name and table alias.
 */
final class BeanEmbeddedMetaFactory {

  /**
   * Create BeanProperties for embedded beans using the deployment specific DB column name and table alias.
   */
  public static BeanEmbeddedMeta create(BeanDescriptorMap owner, DeployBeanPropertyAssocOne<?> prop) {
    // we can get a BeanDescriptor for an Embedded bean
    // and know that it is NOT recursive, as Embedded beans are
    // only allow to hold simple scalar types...
    BeanDescriptor<?> targetDesc = owner.descriptor(prop.getTargetType());
    if (targetDesc == null) {
      String msg = "Could not find BeanDescriptor for " + prop.getTargetType()
        + ". Perhaps the EmbeddedId class is not registered? See https://ebean.io/docs/trouble-shooting#not-registered";
      throw new BeanNotRegisteredException(msg);
    }

    // deployment override information (column names)
    String columnPrefix = prop.getColumnPrefix();
    Map<String, Column> propColMap = prop.getDeployEmbedded().getPropertyColumnMap();

    BeanProperty[] sourceProperties = targetDesc.propertiesNonTransient();
    BeanProperty[] embeddedProperties = new BeanProperty[sourceProperties.length];

    for (int i = 0; i < sourceProperties.length; i++) {
      String propertyName = sourceProperties[i].name();
      Column column = propColMap.get(propertyName);
      String dbColumn = dbColumn(columnPrefix, column, sourceProperties[i]);
      boolean dbNullable = dbNullable(column, sourceProperties[i]);
      int dbLength = dbLength(column, sourceProperties[i]);
      int dbScale = dbScale(column, sourceProperties[i]);
      String colDefn = getDbColumnDefn(column, sourceProperties[i]);
      BeanPropertyOverride overrides = new BeanPropertyOverride(dbColumn, dbNullable, dbLength, dbScale, colDefn);
      embeddedProperties[i] = sourceProperties[i].override(overrides);
    }

    return new BeanEmbeddedMeta(embeddedProperties);
  }

  private static String dbColumn(String prefix, Column override, BeanProperty source) {
    String dbCol = (override != null && !override.name().isEmpty()) ? override.name() : source.dbColumn();
    return prefix == null ? dbCol : prefix + dbCol;
  }

  private static boolean dbNullable(Column override, BeanProperty source) {
    return (override != null) ? override.nullable() : source.isNullable();
  }

  private static int dbLength(Column override, BeanProperty source) {
    return (override != null && (override.length() != 0)) ? override.length() : source.dbLength();
  }

  private static int dbScale(Column override, BeanProperty source) {
    return (override != null && (override.scale() != 0)) ? override.scale() : source.dbScale();
  }

  private static String getDbColumnDefn(Column override, BeanProperty source) {
    return (override != null && !override.columnDefinition().isEmpty()) ? override.columnDefinition() : source.dbColumnDefn();
  }

}
