package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.persist.dml.DmlMode;

/**
 * Add base properties to the BindableList for a bean type.
 * <p>
 * This excludes unique embedded and associated properties.
 * </p>
 */
public class FactoryBaseProperties {

  private final FactoryProperty factoryProperty;


  public FactoryBaseProperties(boolean bindEncryptDataFirst) {
    factoryProperty = new FactoryProperty(bindEncryptDataFirst);
  }

  /**
   * Add Bindable for the base properties to the list.
   */
  public void create(List<Bindable> list, BeanDescriptor<?> desc, DmlMode mode, boolean withLobs) {

    add(desc.propertiesBaseScalar(), list, mode, withLobs);

    BeanPropertyCompound[] compoundProps = desc.propertiesBaseCompound();
    for (BeanPropertyCompound compoundProp : compoundProps) {
      BeanProperty[] props = compoundProp.getScalarProperties();

      List<BindableProperty> newList = new ArrayList<>(props.length);
      addCompound(props, newList, mode, withLobs);

      BindableCompound compoundBindable = new BindableCompound(compoundProp, newList);

      list.add(compoundBindable);
    }
  }

  private void add(BeanProperty[] props, List<Bindable> list, DmlMode mode, boolean withLobs) {

    for (BeanProperty prop : props) {
      Bindable item = factoryProperty.create(prop, mode, withLobs);
      if (item != null) {
        list.add(item);
      }
    }
  }

  private void addCompound(BeanProperty[] props, List<BindableProperty> list, DmlMode mode, boolean withLobs) {

    for (BeanProperty prop : props) {
      BindableProperty item = (BindableProperty) factoryProperty.create(prop, mode, withLobs);
      if (item != null) {
        list.add(item);
      }
    }

  }

}
