package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for the synthetic order column.
 */
public class BindableOrderColumn extends BindableProperty {

  public BindableOrderColumn(BeanProperty prop) {
    super(prop);
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    int sortOrder = request.getEntityBeanIntercept().getSortOrder();
    if (sortOrder > 0) {
      list.add(this);
    }
  }

  /**
   * Normal binding of a property value from the bean.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    int sortOrder = bean._ebean_getIntercept().getSortOrder();
    request.bind(sortOrder, prop);
  }

}
