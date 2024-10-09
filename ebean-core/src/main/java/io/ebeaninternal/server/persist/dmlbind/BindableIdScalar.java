package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for a single scalar id property.
 */
final class BindableIdScalar implements BindableId {

  private final BeanProperty uidProp;

  BindableIdScalar(BeanProperty uidProp) {
    this.uidProp = uidProp;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isConcatenated() {
    return false;
  }

  @Override
  public String getIdentityColumn() {
    return uidProp.dbColumn();
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  /**
   * Does nothing for BindableId.
   */
  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  /**
   * Should not be called as this is really only for concatenated keys.
   */
  @Override
  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    throw new PersistenceException("Should not be called? only for concatinated keys");
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(uidProp.dbColumn());
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    request.appendColumnDefn(uidProp.dbColumn(), uidProp.dbColumnDefn());
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    Object value = uidProp.getValue(bean);
    request.bind(value, uidProp);
    // used for summary logging
    request.setIdValue(value);
  }

}
