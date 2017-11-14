package io.ebeaninternal.server.persist.dml;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.dmlbind.Bindable;
import io.ebeaninternal.server.persist.dmlbind.BindableId;

import java.sql.SQLException;

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
  private final Bindable tenantId;

  private final String tableName;

  private final boolean emptyStringAsNull;

  DeleteMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, BindableId id, Bindable version, Bindable tenantId) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.id = id;
    this.version = version;
    this.tenantId = tenantId;

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

  boolean isEmptyStringAsNull() {
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
    if (tenantId != null) {
      tenantId.dmlBind(bind, bean);
    }

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
    switch (request.getConcurrencyMode()) {
      case NONE:
        return publish ? sqlNone : sqlDraftNone;

      case VERSION:
        return publish ? sqlVersion : sqlDraftVersion;

      default:
        throw new RuntimeException("Invalid mode " + request.getConcurrencyMode());
    }
  }

  private String genSql(ConcurrencyMode conMode, String table) {

    GenerateDmlRequest request = new GenerateDmlRequest();
    request.append("delete from ").append(table);
    request.append(" where ");

    request.setWhereIdMode();
    id.dmlAppend(request);
    if (tenantId != null) {
      tenantId.dmlAppend(request);
    }

    if (ConcurrencyMode.VERSION == conMode) {
      if (version != null) {
        version.dmlAppend(request);
      }
    }

    return request.toString();
  }

}
