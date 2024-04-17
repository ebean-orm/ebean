package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List of Bindable items.
 */
public final class BindableList implements Bindable {

  private final Bindable[] items;

  public BindableList(List<Bindable> list) {
    items = list.toArray(new Bindable[0]);
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

  public boolean isEmpty() {
    return items.length == 0;
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  public void addAll(List<Bindable> list) {
    Collections.addAll(list, items);
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    for (Bindable item : items) {
      item.addToUpdate(request, list);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    for (Bindable item : items) {
      item.dmlAppend(request);
    }
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    for (Bindable item : items) {
      item.dmlType(request);
    }
  }

  @Override
  public void dmlBind(BindableRequest bindRequest, EntityBean bean) throws SQLException {
    for (Bindable item : items) {
      item.dmlBind(bindRequest, bean);
    }
  }

}
