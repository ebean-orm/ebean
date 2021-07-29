package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebean.bean.MutableValueInfo;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.sql.SQLException;

/**
 * For JSON Jackson properties - dirty detection via MD5 of json content.
 */
class BindablePropertyJsonUpdate extends BindableProperty {

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
      // on update store hash and push json
      final String json = bean._ebean_getIntercept().mutableContent(propertyIndex);
      final MutableValueInfo hash = prop.createMutableInfo(json);
      bean._ebean_getIntercept().mutableInfo(propertyIndex, hash);
      request.pushJson(json);
      final Object value = prop.getValue(bean);
      request.bind(value, prop);
    }
  }
}
