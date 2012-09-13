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
package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a concatenated id that is not embedded.
 */
public final class BindableIdMap implements BindableId {

  private final BeanProperty[] uids;

  private final MatchedImportedProperty[] matches;

  public BindableIdMap(BeanProperty[] uids, BeanDescriptor<?> desc) {
    this.uids = uids;
    matches = MatchedImportedProperty.build(uids, desc);
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean isConcatenated() {
    return true;
  }

  public String getIdentityColumn() {
    // return null for concatenated keys
    return null;
  }

  @Override
  public String toString() {
    return Arrays.toString(uids);
  }

  /**
   * Does nothing for BindableId.
   */
  public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  /**
   * Id values are never null in where clause.
   */
  public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
    // id values are never null in where clause
    dmlAppend(request, false);
  }

  public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
    dmlAppend(request, checkIncludes);
  }

  public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
    for (int i = 0; i < uids.length; i++) {
      request.appendColumn(uids[i].getDbColumn());
    }
  }

  public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
    dmlBind(request, checkIncludes, bean, true);
  }

  public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
    dmlBind(request, checkIncludes, bean, false);
  }

  private void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean, boolean bindNull) throws SQLException {

    LinkedHashMap<String, Object> mapId = new LinkedHashMap<String, Object>();
    for (int i = 0; i < uids.length; i++) {
      Object value = uids[i].getValue(bean);

      bindRequest.bind(value, uids[i], uids[i].getName(), bindNull);

      // putting logicalType into map rather than
      // the dbType (which may have been converted).
      mapId.put(uids[i].getName(), value);
    }
    bindRequest.setIdValue(mapId);
  }

  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {

    if (matches == null) {
      String m = "Matches for the concatinated key columns where not found?"
          + " I expect that the concatinated key was null, and this bean does"
          + " not have ManyToOne assoc beans matching the primary key columns?";
      throw new PersistenceException(m);
    }

    Object bean = persist.getBean();

    // populate it from the assoc one id values...
    for (int i = 0; i < matches.length; i++) {
      matches[i].populate(bean, bean);
    }

    return true;
  }

}
