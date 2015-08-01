package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoin;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Represents a join to another table.
 */
public final class TableJoin {

  /**
   * The joined table.
   */
  private final String table;

  /**
   * The type of join as per deployment (cardinality and optionality).
   */
  private final SqlJoinType type;

  private final InheritInfo inheritInfo;
    
  /**
   * Columns as an array.
   */
  private final TableJoinColumn[] columns;

  /**
   * A hash that can be used with the query plan.
   */
  private final int queryHash;

  /**
   * Create a TableJoin.
   */
  public TableJoin(DeployTableJoin deploy) {

    this.table = InternString.intern(deploy.getTable());
    this.type = deploy.getType();
    this.inheritInfo = deploy.getInheritInfo();

    DeployTableJoinColumn[] deployCols = deploy.columns();
    this.columns = new TableJoinColumn[deployCols.length];
    for (int i = 0; i < deployCols.length; i++) {
      this.columns[i] = new TableJoinColumn(deployCols[i]);
    }

    this.queryHash = calcQueryHash();
  }

  /**
   * Calculate a hash value for adding to a query plan.
   */
  private int calcQueryHash() {
    int hc = type.hashCode();
    hc = hc * 31 + (table == null ? 0 : table.hashCode());
    for (int i = 0; i < columns.length; i++) {
      hc = hc * 31 + columns[i].queryHash();
    }
    return hc;
  }

  /**
   * Return a hash value for adding to a query plan.
   */
  public int queryHash() {
    return queryHash;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(30);
    sb.append(type).append(" ").append(table).append(" ");
    for (int i = 0; i < columns.length; i++) {
      sb.append(columns[i]).append(" ");
    }
    return sb.toString();
  }

  /**
   * Return the join columns.
   */
  public TableJoinColumn[] columns() {
    return columns;
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
  public SqlJoinType getType() {
    return type;
  }

  public SqlJoinType addJoin(SqlJoinType joinType, String prefix, DbSqlContext ctx) {

    String[] names = SplitName.split(prefix);
    String a1 = ctx.getTableAlias(names[0]);
    String a2 = ctx.getTableAlias(prefix);

    return addJoin(joinType, a1, a2, ctx);
  }

  public SqlJoinType addJoin(SqlJoinType joinType, String a1, String a2, DbSqlContext ctx) {

   	String inheritance = inheritInfo != null ? inheritInfo.getWhere() : null;

   	String joinLiteral = joinType.getLiteral(type);
   	ctx.addJoin(joinLiteral, table, columns(), a1, a2, inheritance);
        
   	return joinType.autoToOuter(type);
  }
  
}
