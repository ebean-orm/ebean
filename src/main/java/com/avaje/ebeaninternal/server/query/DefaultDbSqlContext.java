package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.HashSet;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.util.ArrayStack;

public class DefaultDbSqlContext implements DbSqlContext {

  private static final String COMMA = ", ";

  private static final String PERIOD = ".";
  private static final int STRING_BUILDER_INITIAL_CAPACITY = 140;

  private final String tableAliasPlaceHolder;

  private final String columnAliasPrefix;

  private final ArrayStack<String> tableAliasStack = new ArrayStack<String>();

  private final ArrayStack<String> joinStack = new ArrayStack<String>();

  private final ArrayStack<String> prefixStack = new ArrayStack<String>();

  private final boolean useColumnAlias;

  private int columnIndex;

  private StringBuilder sb = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);

  /**
   * A Set used to make sure formula joins are only added once to a query.
   */
  private HashSet<String> formulaJoins;

  private HashSet<String> tableJoins;

  private SqlTreeAlias alias;

  private String currentPrefix;

  private ArrayList<BeanProperty> encryptedProps;

  /**
   * Construct for FROM clause (no column alias used).
   */
  public DefaultDbSqlContext(SqlTreeAlias alias, String tableAliasPlaceHolder) {
    this.tableAliasPlaceHolder = tableAliasPlaceHolder;
    this.columnAliasPrefix = null;
    this.useColumnAlias = false;
    this.alias = alias;
  }

  /**
   * Construct for SELECT clause (with column alias settings).
   */
  public DefaultDbSqlContext(SqlTreeAlias alias, String tableAliasPlaceHolder,
      String columnAliasPrefix, boolean alwaysUseColumnAlias) {
    this.alias = alias;
    this.tableAliasPlaceHolder = tableAliasPlaceHolder;
    this.columnAliasPrefix = columnAliasPrefix;
    this.useColumnAlias = alwaysUseColumnAlias;
  }

  public void addEncryptedProp(BeanProperty p) {
    if (encryptedProps == null) {
      encryptedProps = new ArrayList<BeanProperty>();
    }
    encryptedProps.add(p);
  }

  public BeanProperty[] getEncryptedProps() {
    if (encryptedProps == null) {
      return null;
    }

    return encryptedProps.toArray(new BeanProperty[encryptedProps.size()]);
  }

  public String peekJoin() {
    return joinStack.peek();
  }

  public void popJoin() {
    joinStack.pop();
  }

  public void pushJoin(String node) {
    joinStack.push(node);
  }

  public void addJoin(String type, String table, TableJoinColumn[] cols, String a1, String a2, String inheritance) {

    if (tableJoins == null) {
      tableJoins = new HashSet<String>();
    }

    String joinKey = table + "-" + a1 + "-" + a2;
    if (tableJoins.contains(joinKey)) {
      return;
    }

    tableJoins.add(joinKey);

    sb.append(" ");
    sb.append(type);
    sb.append(" ").append(table).append(" ");
    sb.append(a2);
    sb.append(" on ");

    for (int i = 0; i < cols.length; i++) {
      TableJoinColumn pair = cols[i];
      if (i > 0) {
        sb.append(" and ");
      }

      sb.append(a2);
      sb.append(".").append(pair.getForeignDbColumn());
      sb.append(" = ");
      sb.append(a1);
      sb.append(".").append(pair.getLocalDbColumn());
    }


    // add on any inheritance where clause
    if (inheritance != null && inheritance.length() > 0){
    	sb.append(" and ");
    	sb.append(a2);
    	sb.append(".");
    	sb.append(inheritance);
    }

    sb.append(" ");
  }

  public String getTableAlias(String prefix) {
    return alias.getTableAlias(prefix);
  }

  public String getTableAliasManyWhere(String prefix) {
    return alias.getTableAliasManyWhere(prefix);
  }

  public void pushSecondaryTableAlias(String alias) {
    tableAliasStack.push(alias);
  }

  public String getRelativePrefix(String propName) {

    return currentPrefix == null ? propName : currentPrefix + "." + propName;
  }

  public void pushTableAlias(String prefix) {
    prefixStack.push(currentPrefix);
    currentPrefix = prefix;
    tableAliasStack.push(getTableAlias(prefix));
  }

  public void popTableAlias() {
    tableAliasStack.pop();
    currentPrefix = prefixStack.pop();
  }

  public DefaultDbSqlContext append(String s) {
    sb.append(s);
    return this;
  }

  public DefaultDbSqlContext append(char s) {
    sb.append(s);
    return this;
  }

  public void appendFormulaJoin(String sqlFormulaJoin, SqlJoinType joinType) {

    // replace ${ta} place holder with the real table alias...
    String tableAlias = tableAliasStack.peek();
    String converted = StringHelper.replaceString(sqlFormulaJoin, tableAliasPlaceHolder, tableAlias);

    if (formulaJoins == null) {
      formulaJoins = new HashSet<String>();

    } else if (formulaJoins.contains(converted)) {
      // skip adding a formula join because
      // the same join has already been added.
      return;
    }

    // we only want to add this join once
    formulaJoins.add(converted);

    sb.append(" ");
    if (joinType == SqlJoinType.OUTER) {
      if ("join".equals(sqlFormulaJoin.substring(0, 4).toLowerCase())) {
        // prepend left outer as we are in the 'many' part
        append(" left outer ");
      }
    }

    sb.append(converted);
    sb.append(" ");
  }

  public void appendFormulaSelect(String sqlFormulaSelect) {

    String tableAlias = tableAliasStack.peek();
    String converted = StringHelper.replaceString(sqlFormulaSelect, tableAliasPlaceHolder,
        tableAlias);

    sb.append(COMMA);
    sb.append(converted);
  }

  public void appendColumn(String column) {
    appendColumn(tableAliasStack.peek(), column);
  }

  public void appendColumn(String tableAlias, String column) {
    sb.append(COMMA);

    if (column.indexOf("${}") > -1) {
      // support DB functions such as lower() etc
      // with the use of secondary columns
      String x = StringHelper.replaceString(column, "${}", tableAlias);
      sb.append(x);
    } else {
      sb.append(tableAlias);
      sb.append(PERIOD);
      sb.append(column);
    }
    if (useColumnAlias) {
      sb.append(" ");
      sb.append(columnAliasPrefix);
      sb.append(columnIndex);
    }
    columnIndex++;
  }

  public String peekTableAlias() {
    return tableAliasStack.peek();
  }

  public void appendRawColumn(String rawcolumnWithTableAlias) {
    sb.append(COMMA);
    sb.append(rawcolumnWithTableAlias);

    if (useColumnAlias) {
      sb.append(" ");
      sb.append(columnAliasPrefix);
      sb.append(columnIndex);
    }
    columnIndex++;
  }

  public int length() {
    return sb.length();
  }

  public String getContent() {
    String s = sb.toString();
    sb = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);
    return s;
  }

  public String toString() {
    return "DefaultDbSqlContext: " + sb.toString();
  }

}
