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
final class DeleteMeta extends BaseMeta {

  private final String sqlVersion;
  private final String sqlNone;
  private final String sqlDraftVersion;
  private final String sqlDraftNone;

  DeleteMeta(BeanDescriptor<?> desc, BindableId id, Bindable version, Bindable tenantId) {
    super(id, version, tenantId);

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

  /**
   * Bind the request based on the concurrency mode.
   */
  public void bind(PersistRequestBean<?> persist, DmlHandler bind) throws SQLException {

    EntityBean bean = persist.getEntityBean();

    id.dmlBind(bind, bean);
    if (tenantId != null) {
      tenantId.dmlBind(bind, bean);
    }

    if (persist.getConcurrencyMode() == ConcurrencyMode.VERSION) {
      version.dmlBind(bind, bean);
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
    return appendWhere(request, conMode);
  }

}
