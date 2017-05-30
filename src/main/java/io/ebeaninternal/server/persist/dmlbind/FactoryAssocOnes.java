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

    BeanPropertyAssocOne<?>[] ones = desc.propertiesOneImported();

    for (BeanPropertyAssocOne<?> one : ones) {
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
        if (one.getGeneratedProperty() == null) {
          list.add(new BindableAssocOne(one));
        } else {
          // typically generated 'who' created/modified properties
          switch (mode) {
            case INSERT:
              if (one.getGeneratedProperty().includeInInsert()) {
                list.add(new BindableAssocOneGeneratedInsert(one));
              }
              break;
            case UPDATE:
              if (one.getGeneratedProperty().includeInUpdate()) {
                // A 'Who Created property' is never updated
                list.add(new BindableAssocOneGeneratedUpdate(one));
              }
              break;
          }
        }
      }
    }
  }
}
