package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;

/**
 * Used hold meta data when a bean property is overridden.
 * <p>
 * Typically this is for Embedded Beans.
 * </p>
 */
public class BeanPropertyOverride {

	private final String dbColumn;
		
	private final String sqlFormulaSelect;

	private final String sqlFormulaJoin;

	public BeanPropertyOverride(String dbColumn) {
		this(dbColumn, null, null);
	}
	
	public BeanPropertyOverride(String dbColumn, String sqlFormulaSelect, String sqlFormulaJoin) {
		this.dbColumn = InternString.intern(dbColumn);
		this.sqlFormulaSelect = InternString.intern(sqlFormulaSelect);
		this.sqlFormulaJoin = InternString.intern(sqlFormulaJoin);
	}

	public String getDbColumn() {
		return dbColumn;
	}

	public String getSqlFormulaSelect() {
		return sqlFormulaSelect;
	}
	
	public String getSqlFormulaJoin() {
		return sqlFormulaJoin;
	}
	
	public String replace(String src, String srcDbColumn){
	    return StringHelper.replaceString(src, srcDbColumn, dbColumn);
	}
}
