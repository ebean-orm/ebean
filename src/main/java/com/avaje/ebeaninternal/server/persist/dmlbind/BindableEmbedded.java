package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

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

    public String toString() {
        return "BindableEmbedded " + embProp + " items:" + Arrays.toString(items);
    }

    public void dmlAppend(GenerateDmlRequest request) {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlAppend(request);
        }
    }

    public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.isAddToUpdate(embProp)) {
            list.add(this);
        }
    }

    public void dmlBind(BindableRequest bindRequest, EntityBean bean) throws SQLException {

      // get the embedded bean
      EntityBean embBean = (EntityBean)embProp.getValue(bean);
      if (embBean == null) {
        for (int i = 0; i < items.length; i++) {
          items[i].dmlBind(bindRequest, null);
        }
      } else {
        //EntityBeanIntercept ebi = embBean._ebean_getIntercept();
        for (int i = 0; i < items.length; i++) {
          //if (ebi.isLoadedProperty(props[i].getPropertyIndex())) {
            items[i].dmlBind(bindRequest, embBean);
          //}
        }
      }
    }
}
