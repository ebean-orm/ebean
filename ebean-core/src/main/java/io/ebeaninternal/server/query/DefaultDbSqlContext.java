package io.ebeaninternal.server.query;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoinColumn;
import io.ebeaninternal.server.util.ArrayStack;

import java.util.ArrayList;
import java.util.HashSet;

final class DefaultDbSqlContext implements DbSqlContext {

  private static final String COMMA = ", ";
  private static final String PERIOD = ".";
  private static final int STRING_BUILDER_INITIAL_CAPACITY = 140;
  private static final String tableAliasPlaceHolder = "${ta}";
  private static final String tableAliasManyPlaceHolder = "${mta}";

  private final String columnAliasPrefix;

  private final ArrayStack<String> tableAliasStack = new ArrayStack<>();

  private final ArrayStack<String> joinStack = new ArrayStack<>();

  private final ArrayStack<String> prefixStack = new ArrayStack<>();

  private final String fromForUpdate;

  private boolean useColumnAlias;

  private int columnIndex;

  private StringBuilder sb = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);

  /**
   * A Set used to make sure formula joins are only added once to a query.
   */
  private HashSet<String> formulaJoins;

  private HashSet<String> tableJoins;

  private final SqlTreeAlias alias;

  private String currentPrefix;

  private ArrayList<BeanProperty> encryptedProps;

  private final CQueryDraftSupport draftSupport;

  private final CQueryHistorySupport historySupport;

  private final boolean historyQuery;

  /**
   * Construct for SELECT clause (with column alias settings).
   */
  DefaultDbSqlContext(SqlTreeAlias alias, String columnAliasPrefix, CQueryHistorySupport historySupport,
                      CQueryDraftSupport draftSupport, String fromForUpdate) {
    this.alias = alias;
    this.columnAliasPrefix = columnAliasPrefix;
    this.useColumnAlias = columnAliasPrefix != null;
    this.draftSupport = draftSupport;
    this.historySupport = historySupport;
    this.historyQuery = (historySupport != null);
    this.fromForUpdate = fromForUpdate;
  }

  @Override
  public boolean isIncludeSoftDelete() {
    return alias.isIncludeSoftDelete();
  }

  @Override
  public void appendFromForUpdate() {
    if (fromForUpdate != null) {
      append(" ").append(fromForUpdate);
    }
  }

  @Override
  public void startGroupBy() {
    this.useColumnAlias = false;
  }

  @Override
  public void addEncryptedProp(BeanProperty p) {
    if (encryptedProps == null) {
      encryptedProps = new ArrayList<>();
    }
    encryptedProps.add(p);
  }

  @Override
  public BeanProperty[] getEncryptedProps() {
    if (encryptedProps == null) {
      return null;
    }

    return encryptedProps.toArray(new BeanProperty[0]);
  }

  @Override
  public void popJoin() {
    joinStack.pop();
  }

  @Override
  public void pushJoin(String node) {
    joinStack.push(node);
  }

  @Override
  public void addJoin(String type, String table, TableJoinColumn[] cols, String a1, String a2, String extraWhere) {
    if (tableJoins == null) {
      tableJoins = new HashSet<>();
    }
    String joinKey = table + "-" + a1 + "-" + a2;
    if (tableJoins.contains(joinKey)) {
      return;
    }

    tableJoins.add(joinKey);
    sb.append(" ").append(type);
    boolean addAsOfOnClause = false;
    if (draftSupport != null) {
      appendTable(table, draftSupport.getDraftTable(table));
    } else if (!historyQuery) {
      sb.append(" ").append(table).append(" ");
    } else {
      // check if there is an associated history table and if so
      // use the unionAll view - we expect an additional predicate to match
      String asOfView = historySupport.getAsOfView(table);
      appendTable(table, asOfView);
      if (asOfView != null) {
        addAsOfOnClause = !historySupport.isStandardsBased();
      }
    }

    sb.append(a2);
    sb.append(" on ");
    for (int i = 0; i < cols.length; i++) {
      TableJoinColumn pair = cols[i];
      if (i > 0) {
        sb.append(" and ");
      }
      if (pair.getForeignSqlFormula() != null) {
        sb.append(pair.getForeignSqlFormula().replace(tableAliasPlaceHolder, a2));
      } else {
        sb.append(a2).append(".").append(pair.getForeignDbColumn());
      }
      sb.append(" = ");
      if (pair.getLocalSqlFormula() != null) {
        sb.append(pair.getLocalSqlFormula().replace(tableAliasPlaceHolder, a1));
      } else {
        sb.append(a1).append(".").append(pair.getLocalDbColumn());
      }
    }
    if (addAsOfOnClause) {
      sb.append(" and ").append(historySupport.getAsOfPredicate(a2));
    }
    if (extraWhere != null && !extraWhere.isEmpty()) {
      sb.append(" and ");
      // we will also need a many-table alias here
      sb.append(extraWhere.replace(tableAliasPlaceHolder, a2).replace(tableAliasManyPlaceHolder, a1));
    }
  }

  private void appendTable(String table, String draftTable) {
    if (draftTable != null) {
      // there is an associated history table and view so use that
      sb.append(" ").append(draftTable).append(" ");
    } else {
      sb.append(" ").append(table).append(" ");
    }
  }

  @Override
  public boolean isDraftQuery() {
    return draftSupport != null;
  }

  @Override
  public String getTableAlias(String prefix) {
    return alias.getTableAlias(prefix);
  }

  @Override
  public String getTableAliasManyWhere(String prefix) {
    return alias.getTableAliasManyWhere(prefix);
  }

  @Override
  public String getRelativePrefix(String propName) {
    return currentPrefix == null ? propName : currentPrefix + "." + propName;
  }

  @Override
  public void pushTableAlias(String prefix) {
    prefixStack.push(currentPrefix);
    currentPrefix = prefix;
    tableAliasStack.push(getTableAlias(prefix));
  }

  @Override
  public void popTableAlias() {
    tableAliasStack.pop();
    currentPrefix = prefixStack.pop();
  }

  @Override
  public DefaultDbSqlContext append(String s) {
    sb.append(s);
    return this;
  }

  @Override
  public void appendFormulaJoin(String sqlFormulaJoin, SqlJoinType joinType, String manyWhere) {
    // replace ${ta} place holder with the real table alias...
    String tableAlias = manyWhere == null ? tableAliasStack.peek() : getTableAliasManyWhere(manyWhere);
    String converted = sqlFormulaJoin.replace(tableAliasPlaceHolder, tableAlias);
    if (formulaJoins == null) {
      formulaJoins = new HashSet<>();
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
        // prepend left as we are in the 'many' part
        append(" left ");
      }
    }
    sb.append(converted);
  }

  @Override
  public void appendParseSelect(String parseSelect, String columnAlias) {
    String converted = this.alias.parse(parseSelect);
    sb.append(COMMA);
    sb.append(converted);
    if (columnAlias != null) {
      sb.append(" ").append(columnAlias);
    } else {
      appendColumnAlias();
    }
  }

  @Override
  public void appendFormulaSelect(String sqlFormulaSelect) {
    String tableAlias = tableAliasStack.peek();
    sb.append(COMMA);
    sb.append(sqlFormulaSelect.replace(tableAliasPlaceHolder, tableAlias));
    appendColumnAlias();
  }

  @Override
  public void appendHistorySysPeriod() {
    String tableAlias = tableAliasStack.peek();
    sb.append(COMMA);
    sb.append(historySupport.getSysPeriodLower(tableAlias));
    appendColumnAlias();
    sb.append(COMMA);
    sb.append(historySupport.getSysPeriodUpper(tableAlias));
    appendColumnAlias();
  }

  private void appendColumnAlias() {
    if (useColumnAlias) {
      sb.append(" ");
      sb.append(columnAliasPrefix);
      sb.append(columnIndex);
    }
    columnIndex++;
  }

  @Override
  public void appendColumn(String column) {
    appendColumn(tableAliasStack.peek(), column);
  }

  @Override
  public void appendColumn(String tableAlias, String column) {
    sb.append(COMMA);
    if (column.contains("${}")) {
      // support DB functions such as lower() etc
      // with the use of secondary columns
      sb.append(column.replace("${}", tableAlias));
    } else {
      sb.append(tableAlias);
      sb.append(PERIOD);
      sb.append(column);
    }
    appendColumnAlias();
  }

  @Override
  public String peekTableAlias() {
    return tableAliasStack.peek();
  }

  @Override
  public void appendRawColumn(String rawColumnWithTableAlias) {
    sb.append(COMMA);
    sb.append(rawColumnWithTableAlias);
  }

  @Override
  public int length() {
    return sb.length();
  }

  @Override
  public String getContent() {
    String s = sb.toString();
    sb = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);
    return s;
  }

  @Override
  public String toString() {
    return "DefaultDbSqlContext: " + sb;
  }

}
