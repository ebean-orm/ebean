package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.dml.DmlMode;

import java.util.List;

/**
 * A factory that builds Bindable for BeanPropertyAssocOne properties.
 */
public class FactoryAssocOnes {

  public FactoryAssocOnes() {
  }

  /**
   * Add foreign key columns from associated one beans.
   */
  public void create(List<Bindable> list, BeanDescriptor<?> desc, DmlMode mode) {

    for (BeanPropertyAssocOne<?> one : desc.propertiesOneImported()) {
      if (!one.isImportedPrimaryKey()) {
        switch (mode) {
          case INSERT:
            if (!one.isInsertable()) {
              continue;
            }
            break;
          case UPDATE:
            if (!one.isUpdateable()) {
              continue;
            }
            break;
        }
        list.add(new BindableAssocOne(one));
      }
    }
  }
}
