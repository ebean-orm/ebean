package io.ebeaninternal.server.deploy.meta;

import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.query.SqlJoinType;

import javax.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.Set;

/**
 * Represents a join to another table during deployment phase.
 * <p>
 * This gets converted into a immutable TableJoin when complete.
 * </p>
 */
public class DeployTableJoin {

  /**
   * The joined table.
   */
  private String table;

  /**
   * The type of join. LEFT JOIN etc.
   */
  private SqlJoinType type = SqlJoinType.INNER;

  /**
   * The list of join column pairs. Used to generate the on clause.
   */
  private ArrayList<DeployTableJoinColumn> columns = new ArrayList<>(4);

  private InheritInfo inheritInfo;

  /**
   * Create a DeployTableJoin.
   */
  public DeployTableJoin() {
  }

  @Override
  public String toString() {
    return type + " " + table + " " + columns;
  }

  /**
   * Return true if the JoinOnPair have been set.
   */
  public boolean hasJoinColumns() {
    return !columns.isEmpty();
  }

  /**
   * Copy all the columns to this join potentially reversing the columns.
   */
  public void setColumns(DeployTableJoinColumn[] cols, boolean reverse) {
    columns = new ArrayList<>();
    for (DeployTableJoinColumn col : cols) {
      addJoinColumn(col.copy(reverse));
    }
  }

  /**
   * Add a join pair
   */
  public void addJoinColumn(DeployTableJoinColumn pair) {
    columns.add(pair);
  }

  /**
   * Add a JoinColumn
   * <p>
   * The order is generally true for OneToMany and false for ManyToOne relationships.
   * </p>
   */
  public void addJoinColumn(boolean order, JoinColumn jc, BeanTable beanTable) {
    if (!"".equals(jc.table())) {
      setTable(jc.table());
    }
    if (!"".equals(jc.name()) || !"".equals(jc.referencedColumnName())) {
      // only add the join column details when name or referencedColumnName is specified
      addJoinColumn(new DeployTableJoinColumn(order, jc, beanTable));
    }
  }

  /**
   * Add a JoinColumn array.
   */
  public void addJoinColumn(boolean order, JoinColumn[] jcArray, BeanTable beanTable) {
    for (JoinColumn aJcArray : jcArray) {
      addJoinColumn(order, aJcArray, beanTable);
    }
  }

  /**
   * Add a JoinColumn set.
   */
  public void addJoinColumn(boolean order, Set<JoinColumn> joinColumns, BeanTable beanTable) {
    for (JoinColumn joinColumn : joinColumns) {
      addJoinColumn(order, joinColumn, beanTable);
    }
  }

  /**
   * Return the join columns.
   */
  public DeployTableJoinColumn[] columns() {
    return columns.toArray(new DeployTableJoinColumn[columns.size()]);
  }

  /**
   * Return the joined table name.
   */
  public String getTable() {
    return table;
  }

  /**
   * set the joined table name.
   */
  public void setTable(String table) {
    this.table = table;
  }

  /**
   * Return the type of join. LEFT OUTER JOIN etc.
   */
  public SqlJoinType getType() {
    return type;
  }

  public void setType(SqlJoinType type) {
    this.type = type;
  }

  public DeployTableJoin createInverse(String tableName) {

    DeployTableJoin inverse = new DeployTableJoin();
    return copyInternal(inverse, true, tableName, true);
  }

  public void copyTo(DeployTableJoin destJoin, boolean reverse, String tableName) {
    copyInternal(destJoin, reverse, tableName, true);
  }

  public void copyWithoutType(DeployTableJoin destJoin, boolean reverse, String tableName) {
    copyInternal(destJoin, reverse, tableName, false);
  }

  private DeployTableJoin copyInternal(DeployTableJoin destJoin, boolean reverse, String tableName, boolean withType) {

    destJoin.setTable(tableName);
    if (withType) {
      destJoin.setType(type);
    }
    destJoin.setColumns(columns(), reverse);

    return destJoin;
  }

  public InheritInfo getInheritInfo() {
    return inheritInfo;
  }

  public void setInheritInfo(InheritInfo inheritInfo) {
    this.inheritInfo = inheritInfo;
  }

  /**
   * Change the join column (based on imported primary key match on property name etc).
   */
  void setLocalColumn(String dbColumn) {
    if (columns.size() == 1) {
      columns.get(0).setLocalDbColumn(dbColumn);
    }
  }
}
