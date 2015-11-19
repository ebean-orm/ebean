package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a single BeanProperty.
 */
public class BindableProperty implements Bindable {

  protected final BeanProperty prop;

  public BindableProperty(BeanProperty prop) {
    this.prop = prop;
  }

  public String toString() {
    return prop.toString();
  }

  @Override
  public boolean isDraftOnly() {
    return prop.isDraftOnly();
  }

  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(prop)) {
      list.add(this);
    }
  }

  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(prop.getDbColumn());
  }

  /**
   * Normal binding of a property value from the bean.
   */
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object value = null;
    if (bean != null) {
      value = prop.getValue(bean);
    }
    request.bind(value, prop);
  }

  /**
   * For compound types bind one of the underlying scalar values for a compound type.
   */
  public void dmlBindObject(BindableRequest request, Object bean) throws SQLException {

    Object value = null;
    if (bean != null) {
      value = prop.getValueObject(bean);
    }
    request.bind(value, prop);
  }
}
