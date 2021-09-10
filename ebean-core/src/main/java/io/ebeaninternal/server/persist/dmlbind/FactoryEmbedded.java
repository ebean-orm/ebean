package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.dml.DmlMode;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that builds Bindable for embedded bean properties.
 */
public final class FactoryEmbedded {

  private final FactoryProperty factoryProperty;

  public FactoryEmbedded(boolean bindEncryptDataFirst) {
    factoryProperty = new FactoryProperty(bindEncryptDataFirst);
  }

  /**
   * Add bindable for the embedded properties to the list.
   */
  public void create(List<Bindable> list, BeanDescriptor<?> desc, DmlMode mode, boolean withLobs) {
    for (BeanPropertyAssocOne<?> anEmbedded : desc.propertiesEmbedded()) {
      BeanProperty[] props = anEmbedded.properties();
      List<Bindable> bindList = new ArrayList<>(props.length);
      for (BeanProperty prop : props) {
        Bindable item = factoryProperty.create(prop, mode, withLobs, true);
        if (item != null) {
          bindList.add(item);
        }
      }
      list.add(new BindableEmbedded(anEmbedded, bindList));
    }
  }


}
