package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableId;

/**
 * Meta data for delete handler. The meta data is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
public final class DeleteMeta {

  private final String sqlVersion;

  private final String sqlNone;

  private final BindableId id;

  private final Bindable version;

  private final String tableName;

  private final boolean emptyStringAsNull;

  public DeleteMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, BindableId id, Bindable version) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.id = id;
    this.version = version;
    this.sqlNone = genSql(ConcurrencyMode.NONE);
    this.sqlVersion = genSql(ConcurrencyMode.VERSION);
  }

  public boolean isEmptyStringAsNull() {
    return emptyStringAsNull;
  }

  /**
   * Return the table name.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Bind the request based on the concurrency mode.
   */
  public void bind(PersistRequestBean<?> persist, DmlHandler bind) throws SQLException {

    EntityBean bean = persist.getEntityBean();

    id.dmlBind(bind, bean);

    switch (persist.getConcurrencyMode()) {
    case VERSION:
      version.dmlBind(bind, bean);
      break;

    default:
      break;
    }
  }

  /**
   * get or generate the sql based on the concurrency mode.
   */
  public String getSql(PersistRequestBean<?> request) throws SQLException {

    if (id.isEmpty()) {
      throw new IllegalStateException("Can not deleteById on " + request.getFullName() + " as no @Id property");
    }

    switch (request.determineConcurrencyMode()) {
    case NONE:
      return sqlNone;

    case VERSION:
      return sqlVersion;

    default:
      throw new RuntimeException("Invalid mode " + request.determineConcurrencyMode());
    }
  }

  private String genSql(ConcurrencyMode conMode) {

    // delete ... where bcol=? and bc1=? and bc2 is null and ...

    GenerateDmlRequest request = new GenerateDmlRequest(emptyStringAsNull);

    request.append("delete from ").append(tableName);
    request.append(" where ");

    request.setWhereIdMode();
    id.dmlAppend(request);

    if (ConcurrencyMode.VERSION.equals(conMode)) {
      if (version == null) {
        return null;
      }
      version.dmlAppend(request);
    }
    
    return request.toString();
  }

}
