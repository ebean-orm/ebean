package io.ebeaninternal.server.deploy;

import io.ebean.config.BeanNotRegisteredException;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

import java.util.Map;

/**
 * Creates BeanProperties for Embedded beans that have deployment information
 * such as the actual DB column name and table alias.
 */
class BeanEmbeddedMetaFactory {

  /**
   * Create BeanProperties for embedded beans using the deployment specific DB column name and table alias.
   */
  public static BeanEmbeddedMeta create(BeanDescriptorMap owner, DeployBeanPropertyAssocOne<?> prop) {

    // we can get a BeanDescriptor for an Embedded bean
    // and know that it is NOT recursive, as Embedded beans are
    // only allow to hold simple scalar types...
    BeanDescriptor<?> targetDesc = owner.getBeanDescriptor(prop.getTargetType());
    if (targetDesc == null) {
      String msg = "Could not find BeanDescriptor for " + prop.getTargetType()
        + ". Perhaps the EmbeddedId class is not registered? See https://ebean.io/docs/trouble-shooting#not-registered";
      throw new BeanNotRegisteredException(msg);
    }

    // deployment override information (column names)
    String columnPrefix = prop.getColumnPrefix();
    Map<String, String> propColMap = prop.getDeployEmbedded().getPropertyColumnMap();

    BeanProperty[] sourceProperties = targetDesc.propertiesNonTransient();
    BeanProperty[] embeddedProperties = new BeanProperty[sourceProperties.length];

    for (int i = 0; i < sourceProperties.length; i++) {
      String propertyName = sourceProperties[i].getName();
      String dbColumn = propColMap.get(propertyName);
      if (dbColumn == null) {
        // dbColumn not overridden so take original
        dbColumn = sourceProperties[i].getDbColumn();
        if (columnPrefix != null) {
          dbColumn = columnPrefix + dbColumn;
        }
      }

      BeanPropertyOverride overrides = new BeanPropertyOverride(dbColumn);
      if (sourceProperties[i] instanceof BeanPropertyAssocOne) {
        embeddedProperties[i] = new BeanPropertyAssocOne((BeanPropertyAssocOne)sourceProperties[i], overrides);
      } else {
        embeddedProperties[i] = new BeanProperty(sourceProperties[i], overrides);
      }
    }

    return new BeanEmbeddedMeta(embeddedProperties);
  }
}
