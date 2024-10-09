package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebean.bean.MutableValueInfo;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.sql.SQLException;

/**
 * For JSON Jackson properties - dirty detection via MD5 of json content.
 */
final class BindablePropertyJsonInsert extends BindableProperty {

  private final int propertyIndex;

  BindablePropertyJsonInsert(BeanProperty prop) {
    super(prop);
    this.propertyIndex = prop.propertyIndex();
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    if (bean == null) {
      request.bind(null, prop);
    } else {
      Object value = prop.getValue(bean);
      if (value == null) {
        request.bind(null, prop);
      } else {
        // on insert store hash and push json
        final String json = prop.format(value);
        final MutableValueInfo hash = prop.createMutableInfo(value);
        bean._ebean_getIntercept().mutableInfo(propertyIndex, hash);
        request.pushJson(json);
        request.bind(value, prop);
      }
    }
  }
}
