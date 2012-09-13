package com.avaje.ebeaninternal.server.deploy;

import java.util.List;

import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebeaninternal.server.querydefn.SimpleTextParser;

/**
 * Parses sql-select queries to try and determine the location where WHERE and HAVING
 * clauses can be added dynamically to the sql.
 */
public class DRawSqlSelectBuilder {

	public static final String $_AND_HAVING = "${andHaving}";
	
	public static final String $_HAVING = "${having}";

	public static final String $_AND_WHERE = "${andWhere}";

	public static final String $_WHERE = "${where}";

	private static final String ORDER_BY = "order by";

	private final BeanDescriptor<?> desc;

	private final NamingConvention namingConvention;

	private final DRawSqlMeta meta;

	private final boolean debug;

	private String sql;

	private final SimpleTextParser textParser;

	private List<DRawSqlColumnInfo> selectColumns;

	private int placeHolderWhere;
	private int placeHolderAndWhere;
	private int placeHolderHaving;
	private int placeHolderAndHaving;
	private boolean hasPlaceHolders;

	private int selectPos = -1;
	private int fromPos = -1;
	private int wherePos = -1;
	private int groupByPos = -1;
	private int havingPos = -1;
	private int orderByPos = -1;

	private boolean whereExprAnd;
	private int whereExprPos = -1;
	private boolean havingExprAnd;
	private int havingExprPos = -1;

	private String tableAlias;
	
	public DRawSqlSelectBuilder(NamingConvention namingConvention, BeanDescriptor<?> desc, DRawSqlMeta sqlSelectMeta) {

		this.namingConvention = namingConvention;
		this.desc = desc;
		this.tableAlias = sqlSelectMeta.getTableAlias();
		this.meta = sqlSelectMeta;
		this.debug = sqlSelectMeta.isDebug();
		this.sql = sqlSelectMeta.getQuery().trim();
		this.hasPlaceHolders = findAndRemovePlaceHolders();
		this.textParser = new SimpleTextParser(this.sql);
	}

	protected NamingConvention getNamingConvention() {
		return namingConvention;
	}

	protected BeanDescriptor<?> getBeanDescriptor() {
		return desc;
	}
	
	protected boolean isDebug() {
		return debug;
	}

	protected void debug(String msg) {
		if (debug) {
			System.out.println("debug> " + msg);
		}
	}
	
	public DeployNamedQuery parse() {

		if (debug) {
			debug("");
			debug("Parsing sql-select in " + getErrName());
		}

		if (hasPlaceHolders()) {

		} else {
			// parse the sql for the keywords...
			// select, from, where, having, group by, order by
			parseSqlFindKeywords(true);
		}

		selectColumns = findSelectColumns(meta.getColumnMapping());
		whereExprPos = findWhereExprPosition();
		havingExprPos = findHavingExprPosition();

		String preWhereExprSql = removeWhitespace(findPreWhereExprSql());
		String preHavingExprSql = removeWhitespace(findPreHavingExprSql());

		preWhereExprSql = trimSelectKeyword(preWhereExprSql);
		
		String orderBySql = findOrderBySql();	
		
		DRawSqlSelect rawSqlSelect = new DRawSqlSelect(desc, selectColumns, tableAlias, preWhereExprSql, 
				whereExprAnd, preHavingExprSql, havingExprAnd, orderBySql, meta);
		
		return new DeployNamedQuery(rawSqlSelect);
	}

	/**
	 * Find and remove the known place holders such as ${where}.
	 */
	private boolean findAndRemovePlaceHolders() {
		placeHolderWhere = removePlaceHolder($_WHERE);
		placeHolderAndWhere = removePlaceHolder($_AND_WHERE);
		placeHolderHaving = removePlaceHolder($_HAVING);
		placeHolderAndHaving = removePlaceHolder($_AND_HAVING);
		return hasPlaceHolders();
	}

	private int removePlaceHolder(String placeHolder) {
		int pos = sql.indexOf(placeHolder);
		if (pos > -1) {
			int after = pos + placeHolder.length() + 1;
			if (after > sql.length()) {
				sql = sql.substring(0, pos);
			} else {
				sql = sql.substring(0, pos) + sql.substring(after);
			}
		}
		return pos;
	}

	private boolean hasPlaceHolders() {
		if (placeHolderWhere > -1) {
			return true;
		}
		if (placeHolderAndWhere > -1) {
			return true;
		}
		if (placeHolderHaving > -1) {
			return true;
		}
		if (placeHolderAndHaving > -1) {
			return true;
		}
		return false;
	}

	/**
	 * Trim off the select keyword (to support row_number() limit function).
	 */
	private String trimSelectKeyword(String preWhereExprSql) {

		if (preWhereExprSql.length() < 7){
			throw new RuntimeException("Expecting at least 7 chars in ["+preWhereExprSql+"]");
		}
		
		String select = preWhereExprSql.substring(0, 7);
		if (!select.equalsIgnoreCase("select ")){
			throw new RuntimeException("Expecting ["+preWhereExprSql+"] to start with \"select\"");
		}
		return preWhereExprSql.substring(7);
	}
	

	private String findOrderBySql() {
		if (orderByPos > -1) {
			int pos = orderByPos + ORDER_BY.length();
			return sql.substring(pos);
		}
		return null;
	}

	private String findPreHavingExprSql() {
		if (havingExprPos > whereExprPos) {
			// an order by clause follows...
			return sql.substring(whereExprPos, havingExprPos - 1);
		}
		if (whereExprPos > -1) {
			// the rest of the sql...
			return sql.substring(whereExprPos);
		}
		return null;
	}

	private String findPreWhereExprSql() {
		if (whereExprPos > -1) {
			return sql.substring(0, whereExprPos - 1);
		} else {
			return sql;
		}
	}

	protected String getErrName() {
		return "entity[" + desc.getFullName() + "] query[" + meta.getName() + "]";
	}

	/**
	 * Find the columns in the select clause including the table alias' and
	 * column alias.
	 */
	private List<DRawSqlColumnInfo> findSelectColumns(String selectClause) {

		if (selectClause == null || selectClause.trim().length() == 0) {
			if (hasPlaceHolders) {
				if (debug) {
					debug("... No explicit ColumnMapping, so parse the sql looking for SELECT and FROM keywords.");
				}
				parseSqlFindKeywords(false);
			}
			if (selectPos == -1 || fromPos == -1) {
				String msg = "Error in [" + getErrName() + "] parsing sql looking ";
				msg += "for SELECT and FROM keywords.";
				msg += " select:" + selectPos + " from:" + fromPos;
				msg += ".  You could use an explicit columnMapping to bypass this error.";
				throw new RuntimeException(msg);
			}
			selectPos += "select".length();
			selectClause = sql.substring(selectPos, fromPos);
		}

		selectClause = selectClause.trim();
		if (debug) {
			debug("ColumnMapping ... [" + selectClause + "]");
		}

		return new DRawSqlSelectColumnsParser(this,selectClause).parse();
	}

	private void parseSqlFindKeywords(boolean allKeywords) {

		debug("Parsing query looking for SELECT...");
		selectPos = textParser.findWordLower("select");
		if (selectPos == -1) {
			String msg = "Error in "+getErrName()+" parsing sql, can not find SELECT keyword in:";
			throw new RuntimeException(msg + sql);
		}
		debug("Parsing query looking for FROM... SELECT found at " + selectPos);
		fromPos = textParser.findWordLower("from");
		if (fromPos == -1) {
			String msg = "Error in "+getErrName()+" parsing sql, can not find FROM keyword in:";
			throw new RuntimeException(msg + sql);
		}

		if (!allKeywords) {
			return;
		}

		debug("Parsing query looking for WHERE... FROM found at " + fromPos);
		wherePos = textParser.findWordLower("where");
		if (wherePos == -1) {
			debug("Parsing query looking for GROUP... no WHERE found");
			groupByPos = textParser.findWordLower("group", fromPos + 5);
		} else {
			debug("Parsing query looking for GROUP... WHERE found at " + wherePos);
			groupByPos = textParser.findWordLower("group");
		}
		if (groupByPos > -1) {
			debug("Parsing query looking for HAVING... GROUP found at " + groupByPos);
			havingPos = textParser.findWordLower("having");
		}

		int startOrderBy = havingPos;
		if (startOrderBy == -1) {
			startOrderBy = groupByPos;
		}
		if (startOrderBy == -1) {
			startOrderBy = wherePos;
		}
		if (startOrderBy == -1) {
			startOrderBy = fromPos;
		}

		debug("Parsing query looking for ORDER... starting at " + startOrderBy);
		orderByPos = textParser.findWordLower("order", startOrderBy);
	}

	private int findWhereExprPosition() {
		if (hasPlaceHolders) {
			if (placeHolderWhere > -1) {
				return placeHolderWhere;
			} else {
				whereExprAnd = true;
				return placeHolderAndWhere;
			}
		}
		whereExprAnd = wherePos > 0;
		if (groupByPos > 0) {
			return groupByPos;
		}
		if (havingPos > 0) {
			return havingPos;
		}
		if (orderByPos > 0) {
			return orderByPos;
		}
		return -1;
	}

	private int findHavingExprPosition() {
		if (hasPlaceHolders) {
			if (placeHolderHaving > -1) {
				return placeHolderHaving;
			} else {
				havingExprAnd = true;
				return placeHolderAndHaving;
			}
		}
		havingExprAnd = havingPos > 0;
		if (orderByPos > 0) {
			return orderByPos;
		}
		return -1;
	}

	private String removeWhitespace(String sql) {
		if (sql == null) {
			return "";
		}

		boolean removeWhitespace = false;

		int length = sql.length();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = sql.charAt(i);
			if (removeWhitespace) {
				if (!Character.isWhitespace(c)) {
					sb.append(c);
					removeWhitespace = false;
				}
			} else {
				if (c == '\r' || c == '\n') {
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
