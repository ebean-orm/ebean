package com.avaje.ebean;

import com.avaje.ebean.RawSql.Sql;

/**
 * Parses sql-select queries to try and determine the location where WHERE and
 * HAVING clauses can be added dynamically to the sql.
 */
class DRawSqlParser {

  public static final String $_AND_HAVING = "${andHaving}";

  public static final String $_HAVING = "${having}";

  public static final String $_AND_WHERE = "${andWhere}";

  public static final String $_WHERE = "${where}";

  private final SimpleTextParser textParser;

  private String sql;

  private int placeHolderWhere;
  private int placeHolderAndWhere;
  private int placeHolderHaving;
  private int placeHolderAndHaving;
  private final boolean hasPlaceHolders;

  private int selectPos = -1;
  private int distinctPos = -1;
  private int fromPos = -1;
  private int wherePos = -1;
  private int groupByPos = -1;
  private int havingPos = -1;
  private int orderByPos = -1;
  private int orderByStmtPos = -1;

  private boolean whereExprAnd;
  private int whereExprPos = -1;
  private boolean havingExprAnd;
  private int havingExprPos = -1;

  public static Sql parse(String sql) {
    return new DRawSqlParser(sql).parse();
  }

  private DRawSqlParser(String sqlString) {
    sqlString = sqlString.trim();
    this.sql = sqlString;
    this.hasPlaceHolders = findAndRemovePlaceHolders();
    this.textParser = new SimpleTextParser(sqlString);
  }

  private Sql parse() {

    if (!hasPlaceHolders()) {
      // parse the sql for the keywords...
      // select, from, where, having, group by, order by
      parseSqlFindKeywords(true);
    }

    whereExprPos = findWhereExprPosition();
    havingExprPos = findHavingExprPosition();

    String preFrom = removeWhitespace(findPreFromSql());
    String preWhere = removeWhitespace(findPreWhereSql());
    String preHaving = removeWhitespace(findPreHavingSql());
    String orderByPrefix = findOrderByPrefixSql();
    String orderBySql = findOrderBySql();

    preFrom = trimSelectKeyword(preFrom);

    return new Sql(sql.hashCode(), preFrom, preWhere, whereExprAnd, preHaving, havingExprAnd, orderByPrefix, orderBySql, (distinctPos > -1));
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
    return placeHolderWhere > -1 || placeHolderAndWhere > -1 || placeHolderHaving > -1 || placeHolderAndHaving > -1;
  }

  /**
   * Trim off the select keyword (to support row_number() limit function).
   */
  private String trimSelectKeyword(String preWhereExprSql) {

    if (selectPos < 0) {
      throw new IllegalStateException("select keyword not found?");
    }
    // trim of select keyword
    preWhereExprSql = preWhereExprSql.trim();
    String select = preWhereExprSql.substring(0, 7);
    if (!select.equalsIgnoreCase("select ")) {
      throw new RuntimeException("Expecting [" + preWhereExprSql + "] to start with \"select\"");
    }
    preWhereExprSql = preWhereExprSql.substring(7).trim();
    if (distinctPos > -1) {
      // trim of distinct keyword
      String distinct = preWhereExprSql.substring(0, 9);
      if (!distinct.equalsIgnoreCase("distinct ")) {
        throw new RuntimeException("Expecting [" + preWhereExprSql
            + "] to start with \"select distinct\"");
      }
      preWhereExprSql = preWhereExprSql.substring(9);
    }

    return preWhereExprSql;
  }

  private String findOrderByPrefixSql() {
    return (orderByPos < 1) ? null : sql.substring(orderByPos, orderByStmtPos);
  }

  private String findOrderBySql() {
    return (orderByStmtPos < 1) ? null : sql.substring(orderByStmtPos).trim();
  }

  private String findPreHavingSql() {
    if (havingExprPos > whereExprPos) {
      // an order by clause follows...
      return sql.substring(whereExprPos, havingExprPos - 1);
    }
    if (whereExprPos > -1) {
      if (orderByPos == -1) {
        return sql.substring(whereExprPos);

      } else if (whereExprPos == orderByPos) {
        return "";

      } else {
        return sql.substring(whereExprPos, orderByPos - 1);
      }
    }
    return null;
  }

  private String findPreFromSql() {
    return sql.substring(0, fromPos - 1);
  }

  private String findPreWhereSql() {
    if (whereExprPos > -1) {
      return sql.substring(fromPos, whereExprPos - 1);
    } else {
      return sql.substring(fromPos);
    }
  }

  private void parseSqlFindKeywords(boolean allKeywords) {

    selectPos = textParser.findWordLower("select");
    if (selectPos == -1) {
      String msg = "Error parsing sql, can not find SELECT keyword in:";
      throw new RuntimeException(msg + sql);
    }

    String possibleDistinct = textParser.nextWord();
    if ("distinct".equals(possibleDistinct)) {
      distinctPos = textParser.getPos() - 8;
    }

    fromPos = textParser.findWordLower("from");
    if (fromPos == -1) {
      String msg = "Error parsing sql, can not find FROM keyword in:";
      throw new RuntimeException(msg + sql);
    }

    if (!allKeywords) {
      return;
    }

    wherePos = textParser.findWordLower("where");
    if (wherePos == -1) {
      groupByPos = textParser.findWordLower("group", fromPos + 5);
    } else {
      groupByPos = textParser.findWordLower("group");
    }
    if (groupByPos > -1) {
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

    orderByPos = textParser.findWordLower("order", startOrderBy);
    if (orderByPos > 1) {
      // there might be keywords like siblings in between the order
      // and by so search for the by keyword explicitly
      orderByStmtPos = 2 + textParser.findWordLower("by", orderByPos);
    }
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

    String s = sb.toString();
    return s.trim();
  }
}
