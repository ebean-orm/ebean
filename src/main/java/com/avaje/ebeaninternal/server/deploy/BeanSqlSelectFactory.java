package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.deploy.BeanSqlSelect.PredicatesType;

public class BeanSqlSelectFactory {

	public static BeanSqlSelect create(String sql){
		
		sql = trim(sql);
		
		PredicatesType predicatesType = determinePredicatesType(sql);
		boolean hasOrderBy  = determineHasOrderBy(sql);
		
		return new BeanSqlSelect(sql, predicatesType, hasOrderBy);
	}
	
	private static boolean determineHasOrderBy(String sql){
		return sql.indexOf(BeanSqlSelect.ORDER_BY) > 0;
	}
	
	private static PredicatesType determinePredicatesType(String sql){
		if (sql.indexOf(BeanSqlSelect.HAVING_PREDICATES) > 0){
			return PredicatesType.HAVING;
		}
		if (sql.indexOf(BeanSqlSelect.WHERE_PREDICATES) > 0){
			return PredicatesType.WHERE;
		}
		if (sql.indexOf(BeanSqlSelect.AND_PREDICATES) > 0){
			return PredicatesType.AND;
		}
		return PredicatesType.NONE;
	}
	
	private static String trim(String sql) {
		
		boolean removeWhitespace = false;
		
		int length = sql.length();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = sql.charAt(i);
			if (removeWhitespace){
				if (!Character.isWhitespace(c)){
					sb.append(c);
					removeWhitespace = false;
				}
			} else {
				if (c == '\r' || c == '\n'){
					sb.append('\n');
					removeWhitespace = true;
				} else {
					sb.append(c);
				}
			}
		}
		
		return sb.toString();
	}
}
