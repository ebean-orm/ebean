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
package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Join to Many (or child of a many) to support where clause predicates on many properties.
 * 
 * @author rbygrave
 */
public class SqlTreeNodeManyWhereJoin implements SqlTreeNode {

    private final String parentPrefix;
    private final String prefix;
    private final BeanPropertyAssoc<?> nodeBeanProp;
    private final SqlTreeNode[] children;

    public SqlTreeNodeManyWhereJoin(String prefix, BeanPropertyAssoc<?> prop) {
       
        this.nodeBeanProp = prop;
        this.prefix = prefix;

        String[] split = SplitName.split(prefix);
        this.parentPrefix = split[0];
        
        List<SqlTreeNode> childrenList = new ArrayList<SqlTreeNode>(0);
        this.children = childrenList.toArray(new SqlTreeNode[childrenList.size()]);
    }

    /**
     * Append to the FROM clause for this node.
     */
    public void appendFrom(DbSqlContext ctx, boolean forceOuterJoin) {
        
        appendFromBaseTable(ctx, forceOuterJoin);
        
        for (int i = 0; i < children.length; i++) {
            children[i].appendFrom(ctx, forceOuterJoin);
        }
    }

    /**
     * Join to base table for this node. This includes a join to the
     * intersection table if this is a ManyToMany node.
     */
    public void appendFromBaseTable(DbSqlContext ctx, boolean forceOuterJoin) {

        String alias = ctx.getTableAliasManyWhere(prefix);
        String parentAlias = ctx.getTableAliasManyWhere(parentPrefix);

        if (nodeBeanProp instanceof BeanPropertyAssocOne<?>){
            nodeBeanProp.addInnerJoin(parentAlias, alias, ctx);
            
        } else {
            BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>)nodeBeanProp;
            if (!manyProp.isManyToMany()) {
                manyProp.addInnerJoin(parentAlias, alias, ctx);
    
            } else {
                String alias2 = alias + "z_";
    
                TableJoin manyToManyJoin = manyProp.getIntersectionTableJoin();
                manyToManyJoin.addInnerJoin(parentAlias, alias2, ctx);
                manyProp.addInnerJoin(alias2, alias, ctx);
            }
        }
    }

    public void buildSelectExpressionChain(List<String> selectChain) {
        // nothing to add
    }

    public void appendSelect(DbSqlContext ctx, boolean subQuery) {
        // nothing to do here
    }

    public void appendWhere(DbSqlContext ctx) {
        // nothing to do here
    }

    public void load(DbReadContext ctx, Object parentBean) throws SQLException {
        // nothing to do here
    }

}
