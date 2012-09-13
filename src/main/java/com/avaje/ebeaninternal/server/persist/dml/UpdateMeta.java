/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.core.ConcurrencyMode;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
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

  private final Bindable set;
  private final BindableId id;
  private final Bindable version;
  private final Bindable all;

  private final String tableName;

  private final UpdatePlan modeNoneUpdatePlan;
  private final UpdatePlan modeVersionUpdatePlan;

  private final boolean emptyStringAsNull;

  public UpdateMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, Bindable set, BindableId id, Bindable version, Bindable all) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.set = set;
    this.id = id;
    this.version = version;
    this.all = all;

    this.sqlNone = genSql(ConcurrencyMode.NONE, null, null);
    this.sqlVersion = genSql(ConcurrencyMode.VERSION, null, null);

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

    Object bean = persist.getBean();

    bind.bindLogAppend(" set[");
    // bind.setCheckDelta(true);
    updatePlan.bindSet(bind, bean);
    // bind.setCheckDelta(false);

    bind.bindLogAppend("] where[");
    id.dmlBind(bind, false, bean);

    switch (persist.getConcurrencyMode()) {
    case VERSION:
      version.dmlBind(bind, false, bean);
      break;
    case ALL:
      Object oldBean = persist.getOldValues();
      all.dmlBindWhere(bind, true, oldBean);
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

    case ALL:
      Object oldValues = request.getOldValues();
      if (oldValues == null) {
        throw new PersistenceException("OldValues are null?");
      }
      String sql = genDynamicWhere(request.getUpdatedProperties(), request.getLoadedProperties(), oldValues);
      return new UpdatePlan(ConcurrencyMode.ALL, sql, set);

    default:
      throw new RuntimeException("Invalid mode " + mode);
    }
  }

  private SpiUpdatePlan getDynamicUpdatePlan(ConcurrencyMode mode, PersistRequestBean<?> persistRequest) {

    Set<String> updatedProps = persistRequest.getUpdatedProperties();

    if (ConcurrencyMode.ALL.equals(mode)) {
      // due to is null in where clause we won't bother trying to
      // cache plans for ConcurrencyMode.ALL
      String sql = genSql(mode, persistRequest, null);
      if (sql == null) {
        // changed properties must have been updatable=false
        return UpdatePlan.EMPTY_SET_CLAUSE;
      } else {
        return new UpdatePlan(null, mode, sql, set, updatedProps);
      }
    }

    // we can use a cached UpdatePlan for the changed properties
    int hash = mode.hashCode();
    hash = hash * 31 + (updatedProps == null ? 0 : updatedProps.hashCode());
    Integer key = Integer.valueOf(hash);

    BeanDescriptor<?> beanDescriptor = persistRequest.getBeanDescriptor();
    SpiUpdatePlan updatePlan = beanDescriptor.getUpdatePlan(key);
    if (updatePlan != null) {
      return updatePlan;
    }

    // build a new UpdatePlan and cache it

    // build a bindableList that only contains the changed properties
    List<Bindable> list = new ArrayList<Bindable>();
    set.addChanged(persistRequest, list);
    BindableList bindableList = new BindableList(list);

    // build the SQL for this update statement
    String sql = genSql(mode, persistRequest, bindableList);

    updatePlan = new UpdatePlan(key, mode, sql, bindableList, null);

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
    if (bindableList != null) {
      bindableList.dmlAppend(request, false);
    } else {
      set.dmlAppend(request, true);
    }

    if (request.getBindColumnCount() == 0) {
      // update properties must have been updatable=false
      // with the result that nothing is in the set clause
      return null;
    }

    request.append(" where ");

    request.setWhereIdMode();
    id.dmlAppend(request, false);

    if (ConcurrencyMode.VERSION.equals(conMode)) {
      if (version == null) {
        return null;
      }
      version.dmlAppend(request, false);

    } else if (ConcurrencyMode.ALL.equals(conMode)) {

      all.dmlWhere(request, true, request.getOldValues());
    }

    return request.toString();
  }

  /**
   * Generate the sql dynamically for where using IS NULL for binding null
   * values.
   */
  private String genDynamicWhere(Set<String> loadedProps, Set<String> whereProps, Object oldBean) {

    // always has a preceding id property(s) so the first
    // option is always ' and ' and not blank.

    GenerateDmlRequest request = new GenerateDmlRequest(emptyStringAsNull, loadedProps, whereProps, oldBean);

    request.append(sqlNone);

    request.setWhereMode();
    all.dmlWhere(request, true, oldBean);

    return request.toString();
  }

}
