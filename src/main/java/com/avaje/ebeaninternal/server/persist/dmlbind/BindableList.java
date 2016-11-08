package com.avaje.ebeaninternal.server.persist.dmlbind;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List of Bindable items.
 */
public class BindableList implements Bindable {

  private final Bindable[] items;

  public BindableList(List<Bindable> list) {
    items = list.toArray(new Bindable[list.size()]);
  }

  /**
   * Return a bindable list that excludes @DraftOnly properties.
   */
  public BindableList excludeDraftOnly() {
    List<Bindable> copy = new ArrayList<>(items.length);
    for (Bindable b : items) {
      if (!b.isDraftOnly()) {
        copy.add(b);
      }
    }
    return new BindableList(copy);
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  public void addAll(List<Bindable> list) {
    Collections.addAll(list, items);
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
