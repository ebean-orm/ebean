package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

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
    
    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        for (int i = 0; i < items.length; i++) {
            items[i].addChanged(request, list);
        }
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        for (int i = 0; i < items.length; i++) {
            items[i].dmlInsert(request, checkIncludes);
        }
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlAppend(request, checkIncludes);
        }
    }

    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlWhere(request, checkIncludes, bean);
        }
    }

    public void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean)
            throws SQLException {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBind(bindRequest, checkIncludes, bean);
        }
    }

    public void dmlBindWhere(BindableRequest bindRequest, boolean checkIncludes, Object bean)
            throws SQLException {

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBindWhere(bindRequest, checkIncludes, bean);
        }
    }
}
