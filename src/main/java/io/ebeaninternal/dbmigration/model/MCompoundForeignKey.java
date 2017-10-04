package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.ForeignKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A unique constraint for multiple columns.
 * <p>
 * Note that unique constraint on a single column is instead
 * a boolean flag on the associated MColumn.
 * </p>
 */
public class MCompoundForeignKey {

  private String name;
  private final String referenceTable;
  private final List<String> columns = new ArrayList<>();
  private final List<String> referenceColumns = new ArrayList<>();
  private String indexName;

  public MCompoundForeignKey(String name, String referenceTable, String indexName) {
    this.name = name;
    this.referenceTable = referenceTable;
    this.indexName = indexName;
  }

  /**
   * Add a column pair of local and referenced column.
   */
  public void addColumnPair(String dbCol, String refColumn) {
    columns.add(dbCol);
    referenceColumns.add(refColumn);
  }

  /**
   * Create and return an ForeignKey migration element.
   */
  public ForeignKey createForeignKey() {
    ForeignKey fk = new ForeignKey();
    fk.setName(name);
    fk.setIndexName(indexName);
    fk.setColumnNames(toColumnNames(columns));
    fk.setRefColumnNames(toColumnNames(referenceColumns));
    fk.setRefTableName(referenceTable);
    return fk;
  }
  
  /**
   * Create and return an AlterForeignKey migration element.
   */
  public AlterForeignKey addForeignKey(String tableName) {
    AlterForeignKey fk = new AlterForeignKey();
    fk.setName(name);
    fk.setIndexName(indexName);
    fk.setColumnNames(toColumnNames(columns));
    fk.setRefColumnNames(toColumnNames(referenceColumns));
    fk.setRefTableName(referenceTable);
    fk.setTableName(tableName);
    return fk;
  }
  
  /**
   * Create and return an AlterForeignKey migration element.
   */
  public AlterForeignKey dropForeignKey(String tableName) {
    AlterForeignKey fk = new AlterForeignKey();
    fk.setName(name);
    fk.setIndexName(indexName);
    fk.setColumnNames(DdlHelp.DROP_FOREIGN_KEY);
    fk.setTableName(tableName);
    return fk;
  }

  /**
   * Add a counter to the foreign key and index names to avoid duplication.
   */
  public void addNameSuffix(int counter) {
    this.name = name + "_" + counter;
    this.indexName = indexName + "_" + counter;
  }

  /**
   * Return the foreign key name.
   */
  public String getName() {
    return name;
  }

  /**
   * Return the index name.
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * Return the columns making up the foreign key in order.
   */
  public List<String> getColumns() {
    return columns;
  }

  /**
   * Set the associated index name. Note that setting to null has the effect
   * of indicating an associated index should not be created for this foreign key.
   */
  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  /**
   * Return as an array of string column names.
   */
  private String toColumnNames(List<String> columns) {

    StringBuilder sb = new StringBuilder(40);
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns.get(i));
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(columns, indexName, name, referenceColumns, referenceTable);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof MCompoundForeignKey))
      return false;
    
    MCompoundForeignKey other = (MCompoundForeignKey) obj;
    return Objects.equals(columns, other.columns)
        && Objects.equals(indexName, other.indexName)
        && Objects.equals(name, other.name)
        && Objects.equals(referenceColumns, other.referenceColumns)
        && Objects.equals(referenceTable, other.referenceTable);
  }

}
