package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Bindable for a Embedded bean.
 */
public class BindableEmbedded implements Bindable {

  private final Bindable[] items;

  private final BeanPropertyAssocOne<?> embProp;

  public BindableEmbedded(BeanPropertyAssocOne<?> embProp, List<Bindable> bindList) {
    this.embProp = embProp;
    this.items = bindList.toArray(new Bindable[bindList.size()]);        //this.props = propList.toArray(new BeanProperty[propList.size()]);
  }

  @Override
  public String toString() {
    return "BindableEmbedded " + embProp + " items:" + Arrays.toString(items);
  }

  @Override
  public boolean isDraftOnly() {
    return embProp.isDraftOnly();
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {

    for (Bindable item : items) {
      item.dmlAppend(request);
    }
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(embProp)) {
      list.add(this);
    }
  }

  @Override
  public void dmlBind(BindableRequest bindRequest, EntityBean bean) throws SQLException {

    // get the embedded bean
    EntityBean embBean = (EntityBean) embProp.getValue(bean);
    if (embBean == null) {
      for (Bindable item : items) {
        item.dmlBind(bindRequest, null);
      }
    } else {
      //EntityBeanIntercept ebi = embBean._ebean_getIntercept();
      for (Bindable item : items) {
        //if (ebi.isLoadedProperty(props[i].getPropertyIndex())) {
        item.dmlBind(bindRequest, embBean);
        //}
      }
    }
  }
}
