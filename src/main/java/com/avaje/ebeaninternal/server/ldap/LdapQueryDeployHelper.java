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
package com.avaje.ebeaninternal.server.ldap;

import java.util.ArrayList;
import java.util.Iterator;

import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DeployPropertyParser;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

public class LdapQueryDeployHelper {

    private final LdapOrmQueryRequest<?> request;
    private final SpiQuery<?> query;
    private final BeanDescriptor<?> desc;

    private String filterExpr;
    private Object[] filterValues;
    
    public LdapQueryDeployHelper(LdapOrmQueryRequest<?> request) {
        this.request = request;
        this.query = request.getQuery();
        this.desc = request.getBeanDescriptor();
        
        parse();
    }

    public String[] getSelectedProperties() {

        OrmQueryProperties chunk = query.getDetail().getChunk(null, false);
        if (chunk.allProperties()) {
            return null;
        }
        
        // convert to array of String[] for setReturningAttributes();
        ArrayList<String> ldapSelectProps = new ArrayList<String>();

        Iterator<String> selectProperties = chunk.getSelectProperties();
        while (selectProperties.hasNext()) {
            String propName = selectProperties.next();
            BeanProperty p = desc.getBeanProperty(propName);
            if (p != null) {
                propName = p.getDbColumn();
            }
            ldapSelectProps.add(propName);
        }
        return ldapSelectProps.toArray(new String[ldapSelectProps.size()]);

    }
    
    private void parse() {
 
        DeployPropertyParser deployParser = desc.createDeployPropertyParser();
        
        String baseWhere = query.getAdditionalWhere();
        if (baseWhere != null){
            baseWhere = deployParser.parse(baseWhere);
        }
        
        
        SpiExpressionList<?> whereExp = query.getWhereExpressions();
        if (whereExp != null) {
            
            DefaultExpressionRequest expReq = new DefaultExpressionRequest(request, deployParser);

            ArrayList<?> bindValues = whereExp.buildBindValues(expReq);
            filterValues = bindValues.toArray(new Object[bindValues.size()]);
            String exprWhere = whereExp.buildSql(expReq);
            
            if (baseWhere != null){
                filterExpr = "(&"+baseWhere +exprWhere+")";
            } else {
                filterExpr = exprWhere;
            }
        } else {
            filterExpr = baseWhere;
        }
    }

    public String getFilterExpr() {
        return filterExpr;
    }

    public Object[] getFilterValues() {
        return filterValues;
    }
    
}
