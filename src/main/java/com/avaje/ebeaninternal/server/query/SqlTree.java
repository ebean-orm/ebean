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
package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Represents the SELECT clause part of the SQL query.
 */
public class SqlTree {

    private SqlTreeNode rootNode;

    
    /**
     * Property if resultSet contains master and detail rows.
     */
    private BeanPropertyAssocMany<?> manyProperty;
    private String manyPropertyName;
    private ElPropertyValue manyPropEl;

    private Set<String> includes;

    /**
     * Summary of the select being generated.
     */
    private String summary;

    private String selectSql;

    private String fromSql;

    /**
     * Encrypted Properties require additional binding. 
     */
    private BeanProperty[] encryptedProps;
    
    /**
     * Where clause for inheritance.
     */
    private String inheritanceWhereSql;

    
    /**
     * Create the SqlSelectClause.
     */
    public SqlTree() {
    }
    
    public List<String> buildSelectExpressionChain() {
        ArrayList<String> list = new ArrayList<String>();
        rootNode.buildSelectExpressionChain(list);
        return list;
    }
    
	/**
     * Return the includes. Associated beans lists etc.
     */
    public Set<String> getIncludes() {
        return includes;
    }
    
    /**
     * Set the association includes (Ones and Many's).
     */
    public void setIncludes(Set<String> includes) {
		this.includes = includes;
	}
    
    /**
     * Set the manyProperty used for this query.
     */
	public void setManyProperty(BeanPropertyAssocMany<?> manyProperty, String manyPropertyName, ElPropertyValue manyPropEl) {
		this.manyProperty = manyProperty;
		this.manyPropertyName = manyPropertyName;
		this.manyPropEl = manyPropEl;
	}

    /**
     * Return the String for the actual SQL.
     */
    public String getSelectSql() {
        return selectSql;
    }

    /**
     * Set the select sql clause.
     */
	public void setSelectSql(String selectSql) {
		this.selectSql = selectSql;
	}

	
	public String getFromSql() {
		return fromSql;
	}

	public void setFromSql(String fromSql) {
		this.fromSql = fromSql;
	}
	
	/**
	 * Return the where clause for inheritance.
	 */
	public String getInheritanceWhereSql() {
		return inheritanceWhereSql;
	}

	/**
	 * Set where clause(s) for inheritance.
	 */
	public void setInheritanceWhereSql(String whereSql) {
		this.inheritanceWhereSql = whereSql;
	}

	/**
	 * Set the summary description of the query.
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

    /**
     * Return a summary of the select clause.
     */
    public String getSummary() {
        return summary;
    }
    
    public SqlTreeNode getRootNode() {
    	return rootNode;
    }
    
    public void setRootNode(SqlTreeNode rootNode) {
		this.rootNode = rootNode;
	}

    /**
     * Return the property that is associated with the many. There can only be
     * one per SqlSelect. This can be null.
     */
    public BeanPropertyAssocMany<?> getManyProperty() {
        return manyProperty;
    }

    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public ElPropertyValue getManyPropertyEl() {
        return manyPropEl;
    }

    /**
     * Return true if this query includes a Many association.
     */
    public boolean isManyIncluded() {
        return (manyProperty != null);
    }

    public BeanProperty[] getEncryptedProps() {
        return encryptedProps;
    }

    public void setEncryptedProps(BeanProperty[] encryptedProps) {
        this.encryptedProps = encryptedProps;
    }
}
