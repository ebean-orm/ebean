package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.sql.SQLException;

/**
 * For JSON Jackson properties - dirty detection via MD5 of json content.
 */
final class BindablePropertyJsonUpdate extends BindableProperty {

  private final int propertyIndex;

  BindablePropertyJsonUpdate(BeanProperty prop) {
    super(prop);
    this.propertyIndex = prop.getPropertyIndex();
  }

  /**
   * Normal binding of a property value from the bean.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    if (bean == null) {
      request.bind(null, prop);
    } else {
      // update mutableInfo and push json
      request.pushJson(bean._ebean_getIntercept().mutableNext(propertyIndex));
      request.bind(prop.getValue(bean), prop);
    }
  }
}
