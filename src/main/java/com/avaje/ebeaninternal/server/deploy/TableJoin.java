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
package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoin;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;

import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * Represents a join to another table.
 */
public final class TableJoin {

	
    public static final String NEW_LINE = "\n";
    
    public static final String LEFT_OUTER = "left outer join";

    public static final String JOIN = "join";
    
    /**
     * Flag set when the imported key maps to the primary key.
     * This occurs for intersection tables (ManyToMany).
     */
    private final boolean importedPrimaryKey;
    
    /**
     * The joined table.
     */
    private final String table;
    
    /**
     * The type of join. LEFT OUTER etc.
     */
    private final String type;

    /**
     * The persist cascade info.
     */
    private final BeanCascadeInfo cascadeInfo;
    
    /**
     * Properties as an array.
     */
    private final BeanProperty[] properties;
    
    /**
     * Columns as an array.
     */
    private final TableJoinColumn[] columns;

    /**
     * Create a TableJoin.
     */
    public TableJoin(DeployTableJoin deploy, LinkedHashMap<String,BeanProperty> propMap) {
    	
        this.importedPrimaryKey = deploy.isImportedPrimaryKey();
        this.table = InternString.intern(deploy.getTable());
        this.type = InternString.intern(deploy.getType());
        this.cascadeInfo = deploy.getCascadeInfo();
        
        DeployTableJoinColumn[] deployCols = deploy.columns();
        this.columns = new TableJoinColumn[deployCols.length];
        for (int i = 0; i < deployCols.length; i++) {
			this.columns[i] = new TableJoinColumn(deployCols[i]);
		}
        
        DeployBeanProperty[] deployProps = deploy.properties();
        if (deployProps.length > 0 && propMap == null){
        	throw new NullPointerException("propMap is null?");
        }
        
        this.properties = new BeanProperty[deployProps.length];
        for (int i = 0; i < deployProps.length; i++) {
        	BeanProperty prop = propMap.get(deployProps[i].getName());
        	this.properties[i] = prop;
		}
        
    }

    /**
     * Create a tableJoin based on this object but with different alias.
     */
	public TableJoin createWithAlias(String localAlias, String foreignAlias) {
    
		return new TableJoin(this, localAlias, foreignAlias);
	}
	
	/**
	 * Construct a copy but with different table alias'.
	 */
	private TableJoin(TableJoin join, String localAlias, String foreignAlias){

		// copy the immutable fields
		this.importedPrimaryKey = join.importedPrimaryKey;
		this.table = join.table;
		this.type = join.type;
		this.cascadeInfo = join.cascadeInfo;
		this.properties = join.properties;
		this.columns = join.columns;
	}

		
    public String toString() {
        StringBuilder sb = new StringBuilder(30);
        sb.append(type).append(" ").append(table).append(" ");
        for (int i = 0; i < columns.length; i++) {
			sb.append(columns[i]).append(" ");
		}
        return sb.toString();
    }

    public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    	for (int i = 0, x = properties.length; i < x; i++) {
    		properties[i].appendSelect(ctx, subQuery);
		}
    }
    
    public void load(SqlBeanLoad sqlBeanLoad) throws SQLException {
    	for (int i = 0, x = properties.length; i < x; i++) {
    		properties[i].load(sqlBeanLoad);
		}
    }
    
    public Object readSet(DbReadContext ctx, Object bean, Class<?> type) throws SQLException {
    	for (int i = 0, x = properties.length; i < x; i++) {
    		properties[i].readSet(ctx, bean, type);
		}
    	return null;
    }
    
    /**
     * Return true if the imported foreign key maps to the primary key.
     */
    public boolean isImportedPrimaryKey() {
		return importedPrimaryKey;
	}

    /**
     * Return the persist info.
     */
    public BeanCascadeInfo getCascadeInfo() {
        return cascadeInfo;
    }

    /**
     * Return the join columns.
     */
    public TableJoinColumn[] columns() {
    	return columns;
    }

    
    /**
     * For secondary table joins returns the properties mapped to that table.
     */
    public BeanProperty[] properties() {
    	return properties;
    }

    /**
     * Return the joined table name.
     */
    public String getTable() {
        return table;
    }

    /**
     * Return the type of join. LEFT OUTER JOIN etc.
     */
    public String getType() {
        return type;
    }

    /**
     * Return true if this join is a left outer join.
     */
    public boolean isOuterJoin() {
        return type.equals(LEFT_OUTER);
    }
    
    public boolean addJoin(boolean forceOuterJoin, String prefix, DbSqlContext ctx) {

    	String[] names = SplitName.split(prefix);
    	String a1 = ctx.getTableAlias(names[0]);
    	String a2 = ctx.getTableAlias(prefix);

    	return addJoin(forceOuterJoin, a1, a2, ctx);
    }
    
    public boolean addJoin(boolean forceOuterJoin, String a1, String a2, DbSqlContext ctx) {
        
        ctx.addJoin(forceOuterJoin?LEFT_OUTER:type, table, columns(), a1, a2);
    	
    	return forceOuterJoin || LEFT_OUTER.equals(type);
    }
    
    /**
     * Explicitly add a (non-outer) join.
     */
    public void addInnerJoin(String a1, String a2, DbSqlContext ctx) {
        ctx.addJoin(JOIN, table, columns(), a1, a2);
    }
}
