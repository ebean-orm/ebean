package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableId;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableList;

/**
 * Meta data for update handler. The meta data is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
public final class UpdateMeta {

  private final String sqlVersion;

  private final String sqlNone;

  private final BindableList set;
  private final BindableId id;
  private final Bindable version;

  private final String tableName;

  private final UpdatePlan modeNoneUpdatePlan;
  private final UpdatePlan modeVersionUpdatePlan;

  private final boolean emptyStringAsNull;

  public UpdateMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, BindableList set, BindableId id, Bindable version) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.set = set;
    this.id = id;
    this.version = version;

    this.sqlNone = genSql(ConcurrencyMode.NONE, null, set);
    this.sqlVersion = genSql(ConcurrencyMode.VERSION, null, set);

    this.modeNoneUpdatePlan = new UpdatePlan(ConcurrencyMode.NONE, sqlNone, set);
    this.modeVersionUpdatePlan = new UpdatePlan(ConcurrencyMode.VERSION, sqlVersion, set);
  }

  /**
   * Return true if empty strings should be treated as null.
   */
  public boolean isEmptyStringAsNull() {
    return emptyStringAsNull;
  }

  /**
   * Return the base table name.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Bind the request based on the concurrency mode.
   */
  public void bind(PersistRequestBean<?> persist, DmlHandler bind, SpiUpdatePlan updatePlan) throws SQLException {

    EntityBean bean = persist.getEntityBean();

    updatePlan.bindSet(bind, bean);

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
  public SpiUpdatePlan getUpdatePlan(PersistRequestBean<?> request) {

    ConcurrencyMode mode = request.determineConcurrencyMode();
    if (request.isDynamicUpdateSql()) {
      return getDynamicUpdatePlan(mode, request);
    }

    // 'full bean' update...
    switch (mode) {
    case NONE:
      return modeNoneUpdatePlan;

    case VERSION:
      return modeVersionUpdatePlan;

    default:
      throw new RuntimeException("Invalid mode " + mode);
    }
  }

  private SpiUpdatePlan getDynamicUpdatePlan(ConcurrencyMode mode, PersistRequestBean<?> persistRequest) {


    // we can use a cached UpdatePlan for the changed properties
    
    EntityBeanIntercept ebi = persistRequest.getEntityBeanIntercept();
    int hash = ebi.getDirtyPropertyHash();
    
    BeanDescriptor<?> beanDescriptor = persistRequest.getBeanDescriptor();
    
    BeanProperty versionProperty = beanDescriptor.getVersionProperty();
    if (versionProperty != null) {
      if (ebi.isLoadedProperty(versionProperty.getPropertyIndex())) {
        hash = hash * 31 + 7;
      }
    }

    Integer key = Integer.valueOf(hash);

    SpiUpdatePlan updatePlan = beanDescriptor.getUpdatePlan(key);
    if (updatePlan != null) {
      return updatePlan;
    }

    // build a new UpdatePlan and cache it

    // build a bindableList that only contains the changed properties
    List<Bindable> list = new ArrayList<Bindable>();
    set.addToUpdate(persistRequest, list);  
    BindableList bindableList = new BindableList(list);

    // build the SQL for this update statement
    String sql = genSql(mode, persistRequest, bindableList);

    updatePlan = new UpdatePlan(key, mode, sql, bindableList);

    // add the UpdatePlan to the cache
    beanDescriptor.putUpdatePlan(key, updatePlan);

    return updatePlan;
  }

  private String genSql(ConcurrencyMode conMode, PersistRequestBean<?> persistRequest, BindableList bindableList) {

    // update set col0=?, col1=?, col2=? where bcol=? and bc1=? and bc2=?

    GenerateDmlRequest request;
    if (persistRequest == null) {
      // For generation of None and Version DML/SQL
      request = new GenerateDmlRequest(emptyStringAsNull);
    } else {
      request = persistRequest.createGenerateDmlRequest(emptyStringAsNull);
    }

    request.append("update ").append(tableName).append(" set ");

    request.setUpdateSetMode();
    bindableList.dmlAppend(request);
    
    if (request.getBindColumnCount() == 0) {
      // update properties must have been updatable=false
      // with the result that nothing is in the set clause
      return null;
    }

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
