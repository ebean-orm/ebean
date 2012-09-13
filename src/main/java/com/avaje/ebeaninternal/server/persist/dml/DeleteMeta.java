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
import java.util.Set;

import com.avaje.ebeaninternal.server.core.ConcurrencyMode;
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

  private final Bindable all;

  private final String tableName;

  private final boolean emptyStringAsNull;

  public DeleteMeta(boolean emptyStringAsNull, BeanDescriptor<?> desc, BindableId id, Bindable version, Bindable all) {
    this.emptyStringAsNull = emptyStringAsNull;
    this.tableName = desc.getBaseTable();
    this.id = id;
    this.version = version;
    this.all = all;

    sqlNone = genSql(ConcurrencyMode.NONE);
    sqlVersion = genSql(ConcurrencyMode.VERSION);
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

    Object bean = persist.getBean();

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
  public String getSql(PersistRequestBean<?> request) throws SQLException {

    if (id.isEmpty()) {
      throw new IllegalStateException("Can not deleteById on " + request.getFullName() + " as no @Id property");
    }

    switch (request.determineConcurrencyMode()) {
    case NONE:
      return sqlNone;

    case VERSION:
      return sqlVersion;

    case ALL:
      return genDynamicWhere(request.getLoadedProperties(), request.getOldValues());

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
    id.dmlAppend(request, false);

    if (ConcurrencyMode.VERSION.equals(conMode)) {
      if (version == null) {
        return null;
      }
      version.dmlAppend(request, false);

    } else if (ConcurrencyMode.ALL.equals(conMode)) {
      throw new RuntimeException("Never called for ConcurrencyMode.ALL");
    }

    return request.toString();
  }

  /**
   * Generate the sql dynamically for where using IS NULL for binding null
   * values.
   */
  private String genDynamicWhere(Set<String> includedProps, Object oldBean) throws SQLException {

    // always has a preceding id property(s) so the first
    // option is always ' and ' and not blank.

    GenerateDmlRequest request = new GenerateDmlRequest(emptyStringAsNull, includedProps, oldBean);

    request.append(sqlNone);

    request.setWhereMode();
    all.dmlWhere(request, true, oldBean);

    return request.toString();
  }

}
