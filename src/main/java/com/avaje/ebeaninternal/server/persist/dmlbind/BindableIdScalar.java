package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a single scalar id property.
 */
public final class BindableIdScalar implements BindableId {

  private final BeanProperty uidProp;

  public BindableIdScalar(BeanProperty uidProp) {
    this.uidProp = uidProp;
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean isConcatenated() {
    return false;
  }

  public String getIdentityColumn() {
    return uidProp.getDbColumn();
  }

  @Override
  public String toString() {
    return uidProp.toString();
  }

  /**
   * Does nothing for BindableId.
   */
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  /**
   * Should not be called as this is really only for concatenated keys.
   */
  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    throw new PersistenceException("Should not be called? only for concatinated keys");
  }

  public void dmlAppend(GenerateDmlRequest request) {

    request.appendColumn(uidProp.getDbColumn());
  }

  public void dmlBind(BindableRequest request,  EntityBean bean) throws SQLException {

    Object value = uidProp.getValue(bean);

    request.bind(value, uidProp, uidProp.getName());

    // used for summary logging
    request.setIdValue(value);
  }

}
