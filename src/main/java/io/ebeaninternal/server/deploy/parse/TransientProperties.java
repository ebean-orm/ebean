package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

import java.util.List;

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

    List<DeployBeanProperty> props = desc.propertiesBase();
    for (DeployBeanProperty prop : props) {
      if (!prop.isDbRead() && !prop.isDbInsertable() && !prop.isDbUpdateable()) {
        // non-transient...
        prop.setTransient();
      }
    }

    List<DeployBeanPropertyAssocOne<?>> ones = desc.propertiesAssocOne();
    for (DeployBeanPropertyAssocOne<?> prop : ones) {
      if (prop.getBeanTable() == null) {
        if (!prop.isEmbedded()) {
          prop.setTransient();
        }
      }
    }

    List<DeployBeanPropertyAssocMany<?>> manys = desc.propertiesAssocMany();
    for (DeployBeanPropertyAssocMany<?> prop : manys) {
      if (prop.getBeanTable() == null) {
        prop.setTransient();
      }
    }

  }
}
