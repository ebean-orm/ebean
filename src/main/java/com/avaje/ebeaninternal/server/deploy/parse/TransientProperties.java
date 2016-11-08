package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

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
    for (int i = 0; i < props.size(); i++) {
      DeployBeanProperty prop = props.get(i);
      if (!prop.isDbRead() && !prop.isDbInsertable() && !prop.isDbUpdateable()) {
        // non-transient...
        prop.setTransient();
      }
    }

    List<DeployBeanPropertyAssocOne<?>> ones = desc.propertiesAssocOne();
    for (int i = 0; i < ones.size(); i++) {
      DeployBeanPropertyAssocOne<?> prop = ones.get(i);
      if (prop.getBeanTable() == null) {
        if (!prop.isEmbedded()) {
          prop.setTransient();
        }
      }
    }

    List<DeployBeanPropertyAssocMany<?>> manys = desc.propertiesAssocMany();
    for (int i = 0; i < manys.size(); i++) {
      DeployBeanPropertyAssocMany<?> prop = manys.get(i);
      if (prop.getBeanTable() == null) {
        prop.setTransient();
      }
    }

  }
}
