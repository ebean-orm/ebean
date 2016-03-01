package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for update on a property with a GeneratedProperty.
 * <p>
 * This is typically a 'update timestamp' or 'counter'.
 * </p>
 */
public class BindablePropertyUpdateGenerated extends BindableProperty {

  private final GeneratedProperty gen;

  public BindablePropertyUpdateGenerated(BeanProperty prop, GeneratedProperty gen) {
    super(prop);
    this.gen = gen;
  }

  /**
   * Add BindablePropertyUpdateGenerated if the property is loaded.
   */
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (gen.includeInAllUpdates()) {
      list.add(this);
    } else if (request.isLoadedProperty(prop)) {
      list.add(this);
    }
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object value = gen.getUpdateValue(prop, bean, request.now());

    // generated value should be the correct type
    request.bind(value, prop);

    if (prop.isVersion()) {
      if (request.getPersistRequest().isLoadedProperty(prop)) {
        // set to the bean after the where clause has been generated
        request.registerGeneratedVersion(value);
      }
    } else {
      // @WhenModified set without invoking interception
      prop.setValueChanged(bean, value);
    }
  }

  /**
   * Always bind on Insert SET.
   */
  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(prop.getDbColumn());
  }

}
