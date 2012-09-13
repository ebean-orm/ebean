/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

/**
 * Between expression where a value is between two properties.
 * 
 * @author rbygrave
 */
class BetweenPropertyExpression implements SpiExpression {

  private static final long serialVersionUID = 2078918165221454910L;

  private static final String BETWEEN = " between ";

  private final FilterExprPath pathPrefix;
  private final String lowProperty;
  private final String highProperty;
  private final Object value;

  BetweenPropertyExpression(FilterExprPath pathPrefix, String lowProperty, String highProperty, Object value) {
    this.pathPrefix = pathPrefix;
    this.lowProperty = lowProperty;
    this.highProperty = highProperty;
    this.value = value;
  }

  protected String name(String propName) {
    if (pathPrefix == null) {
      return propName;
    } else {
      String path = pathPrefix.getPath();
      if (path == null || path.length() == 0) {
        return propName;
      } else {
        return path + "." + propName;
      }
    }
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }

    elProp = desc.getElPropertyDeploy(name(highProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }
  }

  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(value);
  }

  public void addSql(SpiExpressionRequest request) {

    request.append(" ? ").append(BETWEEN).append(name(lowProperty)).append(" and ").append(name(highProperty));
  }

  public int queryAutoFetchHash() {
    int hc = BetweenPropertyExpression.class.getName().hashCode();
    hc = hc * 31 + lowProperty.hashCode();
    hc = hc * 31 + highProperty.hashCode();
    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    return queryAutoFetchHash();
  }

  public int queryBindHash() {
    return value.hashCode();
  }
}
