package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for a single BeanProperty.
 */
public class BindableProperty implements Bindable {

  protected final BeanProperty prop;

  public BindableProperty(BeanProperty prop) {
    this.prop = prop;
  }

  @Override
  public String toString() {
    return prop.toString();
  }

  @Override
  public boolean isDraftOnly() {
    return prop.isDraftOnly();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(prop)) {
      list.add(this);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(prop.getDbColumn());
  }

  /**
   * Normal binding of a property value from the bean.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object value = null;
    if (bean != null) {
      value = prop.getValue(bean);
    }
    request.bind(value, prop);
  }

}
