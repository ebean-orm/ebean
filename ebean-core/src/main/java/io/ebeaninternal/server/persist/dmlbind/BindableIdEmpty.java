package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.util.List;

final class BindableIdEmpty implements BindableId {

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // nothing
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    // nothing
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    // nothing
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) {
    // nothing
  }

  @Override
  public boolean isConcatenated() {
    return false;
  }

  @Override
  public String getIdentityColumn() {
    return null;
  }

  @Override
  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    return false;
  }

}
