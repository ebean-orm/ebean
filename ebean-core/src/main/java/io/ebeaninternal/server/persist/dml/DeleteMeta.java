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

  DeleteMeta(BeanDescriptor<?> desc, BindableId id, Bindable version, Bindable tenantId) {
    super(id, version, tenantId);
    String tableName = desc.baseTable();
    this.sqlNone = genSql(ConcurrencyMode.NONE, tableName);
    this.sqlVersion = genSql(ConcurrencyMode.VERSION, tableName);
  }

  /**
   * Bind the request based on the concurrency mode.
   */
  void bind(PersistRequestBean<?> persist, DmlHandler bind) throws SQLException {
    EntityBean bean = persist.entityBean();
    id.dmlBind(bind, bean);
    if (tenantId != null) {
      tenantId.dmlBind(bind, bean);
    }

    if (persist.concurrencyMode() == ConcurrencyMode.VERSION) {
      version.dmlBind(bind, bean);
    }
  }

  /**
   * get or generate the sql based on the concurrency mode.
   */
  String getSql(PersistRequestBean<?> request) {
    if (id.isEmpty()) {
      throw new IllegalStateException("Can not deleteById on " + request.fullName() + " as no @Id property");
    }

    switch (request.concurrencyMode()) {
      case NONE:
        return sqlNone;

      case VERSION:
        return sqlVersion;

      default:
        throw new RuntimeException("Invalid mode " + request.concurrencyMode());
    }
  }

  private String genSql(ConcurrencyMode conMode, String table) {
    GenerateDmlRequest request = new GenerateDmlRequest().append("delete from ").append(table).append(" where ");
    return appendWhere(request, conMode);
  }

}
