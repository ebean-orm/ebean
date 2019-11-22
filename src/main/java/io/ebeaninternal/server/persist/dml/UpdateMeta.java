package io.ebeaninternal.server.persist.dml;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.api.SpiUpdatePlan;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.dmlbind.Bindable;
import io.ebeaninternal.server.persist.dmlbind.BindableId;
import io.ebeaninternal.server.persist.dmlbind.BindableList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Meta data for update handler. The meta data is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
final class UpdateMeta extends BaseMeta {

  private final BindableList set;
  private final UpdatePlan modeNoneUpdatePlan;
  private final UpdatePlan modeVersionUpdatePlan;

  UpdateMeta(BeanDescriptor<?> desc, BindableList set, BindableId id, Bindable version, Bindable tenantId) {
    super(id, version, tenantId);
    this.set = set;

    String sqlNone = genSql(ConcurrencyMode.NONE, set, desc.getBaseTable());
    String sqlVersion = genSql(ConcurrencyMode.VERSION, set, desc.getBaseTable());
    this.modeNoneUpdatePlan = new UpdatePlan(ConcurrencyMode.NONE, sqlNone, set);
    this.modeVersionUpdatePlan = new UpdatePlan(ConcurrencyMode.VERSION, sqlVersion, set);
  }

  /**
   * Bind the request based on the concurrency mode.
   */
  public void bind(PersistRequestBean<?> persist, DmlHandler bind, SpiUpdatePlan updatePlan) throws SQLException {

    EntityBean bean = persist.getEntityBean();

    updatePlan.bindSet(bind, bean);

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
  SpiUpdatePlan getUpdatePlan(PersistRequestBean<?> request) {

    if (request.isDynamicUpdateSql()) {
      return getDynamicUpdatePlan(request);
    }

    switch (request.getConcurrencyMode()) {
      case NONE:
        return modeNoneUpdatePlan;

      case VERSION:
        return modeVersionUpdatePlan;

      default:
        throw new RuntimeException("Invalid mode " + request.getConcurrencyMode());
    }
  }

  private SpiUpdatePlan getDynamicUpdatePlan(PersistRequestBean<?> persistRequest) {

    String key = persistRequest.getUpdatePlanHash();

    // check if we can use a cached UpdatePlan
    BeanDescriptor<?> beanDescriptor = persistRequest.getBeanDescriptor();
    SpiUpdatePlan updatePlan = beanDescriptor.getUpdatePlan(key);
    if (updatePlan != null) {
      return updatePlan;
    }

    // build a new UpdatePlan and cache it

    // build a bindableList that only contains the changed properties
    List<Bindable> list = new ArrayList<>();
    set.addToUpdate(persistRequest, list);
    BindableList bindableList = new BindableList(list);

    ConcurrencyMode mode = persistRequest.getConcurrencyMode();

    // build the SQL for this update statement
    String sql = genSql(mode, bindableList, persistRequest.getUpdateTable());

    updatePlan = new UpdatePlan(key, mode, sql, bindableList);

    // add the UpdatePlan to the cache
    beanDescriptor.putUpdatePlan(key, updatePlan);

    return updatePlan;
  }

  private String genSql(ConcurrencyMode conMode, BindableList bindableList, String tableName) {

    GenerateDmlRequest request = new GenerateDmlRequest();
    request.append("update ").append(tableName).append(" set ");
    request.setUpdateSetMode();
    bindableList.dmlAppend(request);

    if (request.getBindColumnCount() == 0) {
      // update properties must have been updatable=false
      // with the result that nothing is in the set clause
      return null;
    }

    request.append(" where ");
    return appendWhere(request, conMode);
  }

}
