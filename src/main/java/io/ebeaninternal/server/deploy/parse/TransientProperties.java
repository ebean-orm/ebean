package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

/**
 * Mark transient properties.
 */
public class TransientProperties {

  public TransientProperties() {
  }

  /**
   * Mark any additional properties as transient.
   */
  public void process(DeployBeanDescriptor<?> desc) {

    for (DeployBeanProperty prop : desc.propertiesBase()) {
      if (!prop.isDbRead() && !prop.isDbInsertable() && !prop.isDbUpdateable()) {
        prop.setTransient();
      }
    }

    for (DeployBeanPropertyAssocOne<?> prop : desc.propertiesAssocOne()) {
      if (prop.getBeanTable() == null && !prop.isEmbedded()) {
        prop.setTransient();
      }
    }

    for (DeployBeanPropertyAssocMany<?> prop : desc.propertiesAssocMany()) {
      if (prop.getBeanTable() == null) {
        prop.setTransient();
      }
    }
  }
}
