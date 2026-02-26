package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for inserting a discriminator value.
 */
public final class BindableDiscriminator implements Bindable {

  private final String columnName;
  private final Object discValue;
  private final int sqlType;

  public BindableDiscriminator(InheritInfo inheritInfo) {
    this.columnName = inheritInfo.getDiscriminatorColumn();
    this.discValue = inheritInfo.getDiscriminatorValue();
    this.sqlType = inheritInfo.getDiscriminatorType();
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    throw new PersistenceException("Never called (only for inserts)");
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(columnName);
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    throw new IllegalArgumentException("Not supported");
  }

  @Override
  public void dmlBind(BindableRequest bindRequest, EntityBean bean) throws SQLException {
    bindRequest.bind(discValue, sqlType);
  }

}
