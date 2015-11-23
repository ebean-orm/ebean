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
  private final String sqlDraftVersion;
  private final String sqlDraftNone;

  private final BindableId id;

  private final Bindable version;

  private final String tableName;

  private final boolean emptyStringAsNull;

  public DeleteMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, BindableId id, Bindable version) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.id = id;
    this.version = version;

    String tableName = desc.getBaseTable();
    this.sqlNone = genSql(ConcurrencyMode.NONE, tableName);
    this.sqlVersion = genSql(ConcurrencyMode.VERSION, tableName);
    if (desc.isDraftable()) {
      String draftTableName = desc.getDraftTable();
      this.sqlDraftNone = genSql(ConcurrencyMode.NONE, draftTableName);
      this.sqlDraftVersion = genSql(ConcurrencyMode.VERSION, draftTableName);

    } else {
      this.sqlDraftNone = sqlNone;
      this.sqlDraftVersion = sqlVersion;
    }
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
  public String getSql(PersistRequestBean<?> request) {

    if (id.isEmpty()) {
      throw new IllegalStateException("Can not deleteById on " + request.getFullName() + " as no @Id property");
    }

    boolean publish = request.isPublish();
    switch (request.determineConcurrencyMode()) {
    case NONE:
      return publish ? sqlNone : sqlDraftNone;

    case VERSION:
      return publish ? sqlVersion : sqlDraftVersion;

    default:
      throw new RuntimeException("Invalid mode " + request.determineConcurrencyMode());
    }
  }

  private String genSql(ConcurrencyMode conMode, String table) {

    // delete ... where bcol=? and bc1=? and bc2 is null and ...

    GenerateDmlRequest request = new GenerateDmlRequest();

    request.append("delete from ").append(table);
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
