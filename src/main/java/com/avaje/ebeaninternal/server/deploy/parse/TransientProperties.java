package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.List;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

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
