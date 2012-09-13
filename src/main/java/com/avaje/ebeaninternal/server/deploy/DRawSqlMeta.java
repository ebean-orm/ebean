package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.annotation.SqlSelect;

/**
 * Meta data for a sql-select object.
 * <p>
 * Created from SqlSelect annotation or xml deployment.
 * </p>
 */
public class DRawSqlMeta {

	private String name;
	private String tableAlias;
	private String extend;
	private String query;
	private boolean debug;
	private String where;
	private String having;
	private String columnMapping;


	public DRawSqlMeta(SqlSelect sqlSelect) {
		this.debug = sqlSelect.debug();
		this.name = sqlSelect.name();
		this.tableAlias = toNull(sqlSelect.tableAlias());
		this.extend = toNull(sqlSelect.extend());
		this.having = toNull(sqlSelect.having());
		this.where = toNull(sqlSelect.where());
		this.columnMapping = toNull(sqlSelect.columnMapping());
		this.query = toNull(sqlSelect.query());
	}

	public DRawSqlMeta(String name, String extend, String query, boolean debug,
			String where, String having, String columnMapping) {

		this.name = name;
		this.extend = extend;
		this.query = query;
		this.debug = debug;
		this.having = having;
		this.where = where;
		this.columnMapping = columnMapping;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getHaving() {
		return having;
	}

	public void setHaving(String having) {
		this.having = having;
	}

	public String getColumnMapping() {
		return columnMapping;
	}

	public void setColumnMapping(String columnMapping) {
		this.columnMapping = columnMapping;
	}

	public void extend(DRawSqlMeta parentQuery){
		extendQuery(parentQuery.getQuery());
		extendColumnMapping(parentQuery.getColumnMapping());
	}
	
	/**
	 * Prepend sql from the parent query that this query 'extends'.
	 */
	private void extendQuery(String parentSql) {
		if (query == null) {
			query = parentSql;
		} else {
			query = parentSql + " " + query;
		}
	}

	private void extendColumnMapping(String parentColumnMapping) {
		if (columnMapping == null){
			columnMapping = parentColumnMapping;
		}
	}

	private static String toNull(String s) {
		if (s != null && s.equals("")){
			return null;
		} else {
			return s;
		}
	}
}