package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;

/**
 * Bindable for insert on a property with a GeneratedProperty.
 * <p>
 * This is typically a 'insert timestamp', 'update timestamp' or 'counter'.
 * </p>
 */
public class BindablePropertyInsertGenerated extends BindableProperty {

  private final GeneratedProperty gen;

  public BindablePropertyInsertGenerated(BeanProperty prop, GeneratedProperty gen) {
    super(prop);
    this.gen = gen;
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object value = gen.getInsertValue(prop, bean, request.now());

    // generated value should be the correct type
    if (bean != null) {
      // support PropertyChangeSupport
      //prop.setValueIntercept(bean, value);
      prop.setValue(bean, value);
    }
    request.bind(value, prop);
  }

  /**
   * Always bind on Insert SET.
   */
  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(prop.getDbColumn());
  }

}
