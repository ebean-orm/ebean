package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

public class BindableIdEmpty implements BindableId {
  
  public boolean isEmpty() {
    return true;
  }

  public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
    // nothing
  }

  public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
    // nothing
  }

  public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
    // nothing
  }

  public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
    // nothing
  }

  public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
    // nothing
  }

  public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
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
