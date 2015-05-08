package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * List of Bindable items.
 */
public class BindableList implements Bindable {

    private final Bindable[] items;

    public BindableList(List<Bindable> list) {
        items = list.toArray(new Bindable[list.size()]);
    }

    public void addAll(List<Bindable> list) {
      for (int i = 0; i < items.length; i++) {
        list.add(items[i]);
      }
    }
    
    public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
        for (int i = 0; i < items.length; i++) {
            items[i].addToUpdate(request, list);
        }
    }

    public void dmlAppend(GenerateDmlRequest request) {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlAppend(request);
        }
    }

    public void dmlBind(BindableRequest bindRequest, EntityBean bean)
            throws SQLException {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBind(bindRequest, bean);
        }
    }

}
