package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.util.Md5;

import java.sql.SQLException;

/**
 * For JSON Jackson properties - dirty detection via MD5 of json content.
 */
class BindablePropertyJsonInsert extends BindableProperty {

  private final int propertyIndex;

  BindablePropertyJsonInsert(BeanProperty prop) {
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
      Object value = prop.getValue(bean);
      if (value == null) {
        request.bind(null, prop);
      } else {
        // on insert store MD5 hash and push json
        final String json = prop.format(value);
        bean._ebean_getIntercept().mutableHash(propertyIndex, prop.getScalarType().jsonMutable(json));
        request.pushJson(json);
        request.bind(value, prop);
      }
    }
  }
}
