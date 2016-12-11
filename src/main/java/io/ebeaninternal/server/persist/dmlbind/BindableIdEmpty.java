package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;

public class BindableIdEmpty implements BindableId {

  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // nothing
  }

  public void dmlAppend(GenerateDmlRequest request) {
    // nothing
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    // nothing
  }

  public boolean isConcatenated() {
    return false;
  }

  public String getIdentityColumn() {
    return null;
  }

  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    return false;
  }

}
