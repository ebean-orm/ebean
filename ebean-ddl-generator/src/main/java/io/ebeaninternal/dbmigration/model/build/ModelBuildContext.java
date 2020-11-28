package io.ebeaninternal.dbmigration.model.build;

import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DefaultConstraintMaxLength;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MCompoundForeignKey;
import io.ebeaninternal.dbmigration.model.MIndex;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.TableJoinColumn;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The context used during DDL generation.
 */
public class ModelBuildContext {

  /**
   * Use platform agnostic logical types. These types are converted to
   * platform specific types in the DDL generation.
   */
  private final DbPlatformTypeMapping dbTypeMap = DbPlatformTypeMapping.logicalTypes();

  private final ModelContainer model;

  private final DatabasePlatform databasePlatform;

  private final DbConstraintNaming constraintNaming;

  private final DbConstraintNaming.MaxLength maxLength;

  private final boolean platformTypes;

  public ModelBuildContext(ModelContainer model, DatabasePlatform databasePlatform, DbConstraintNaming naming, boolean platformTypes) {
    this.model = model;
    this.databasePlatform = databasePlatform;
    this.constraintNaming = naming;
    this.platformTypes = platformTypes;
    this.maxLength = maxLength();
  }

  /**
   * Create the max length handling for constraint names.
   */
  private DbConstraintNaming.MaxLength maxLength() {
    if (constraintNaming.getMaxLength() != null) {
      return constraintNaming.getMaxLength();
    }
    return new DefaultConstraintMaxLength(databasePlatform.getMaxConstraintNameLength());
  }

  /**
   * Adjust the foreign key references on any draft tables (that reference other draft tables).
   * This is called as a 'second pass' after all the draft tables have been identified.
   */
  public void adjustDraftReferences() {
    model.adjustDraftReferences();
  }

  public String normaliseTable(String baseTable) {
    return constraintNaming.normaliseTable(baseTable);
  }

  /**
   * Take into account max length and quoted identifiers in constraint and index names.
   */
  private String name(String constraintName, int indexCount) {
    return databasePlatform.convertQuotedIdentifiers(maxLength(constraintName, indexCount));
  }

  private String maxLength(String constraintName, int indexCount) {
    return maxLength.maxLength(constraintName, indexCount);
  }

  public String primaryKeyName(String tableName) {
    return name(constraintNaming.primaryKeyName(tableName), 0);
  }

  public String foreignKeyConstraintName(String tableName, String columnName, int foreignKeyCount) {
    return name(constraintNaming.foreignKeyConstraintName(tableName, columnName), foreignKeyCount);
  }

  public String foreignKeyIndexName(String tableName, String[] columns, int indexCount) {
    return name(constraintNaming.foreignKeyIndexName(tableName, columns), indexCount);
  }

  public String foreignKeyIndexName(String tableName, String column, int indexCount) {
    return name(constraintNaming.foreignKeyIndexName(tableName, column), indexCount);
  }

  public String indexName(String tableName, String column, int indexCount) {
    return name(constraintNaming.indexName(tableName, column), indexCount);
  }

  public String indexName(String tableName, String[] columns, int indexCount) {
    return name(constraintNaming.indexName(tableName, columns), indexCount);
  }

  public String uniqueConstraintName(String tableName, String columnName, int indexCount) {
    return name(constraintNaming.uniqueConstraintName(tableName, columnName), indexCount);
  }

  public String uniqueConstraintName(String tableName, String[] columnNames, int indexCount) {
    return name(constraintNaming.uniqueConstraintName(tableName, columnNames), indexCount);
  }

  public String checkConstraintName(String tableName, String columnName, int checkCount) {
    return name(constraintNaming.checkConstraintName(tableName, columnName), checkCount);
  }

  public MTable addTable(MTable table) {
    return model.addTable(table);
  }

  public void addTableElementCollection(MTable table) {
    model.addTableElementCollection(table);
  }

  public void addIndex(MIndex index) {
    model.addIndex(index);
  }

  /**
   * Return the map used to determine the DB specific type
   * for a given bean property.
   */
  public DbPlatformTypeMapping getDbTypeMap() {
    return dbTypeMap;
  }


  /**
   * Render the DB type for this property given the strict mode.
   */
  public String getColumnDefn(BeanProperty p, boolean strict) {
    DbPlatformType dbType = getDbType(p);
    if (dbType == null) {
      throw new IllegalStateException("Unknown DbType mapping for " + p.getFullBeanName());
    }
    return p.renderDbType(dbType, strict);
  }

  private DbPlatformType getDbType(BeanProperty p) {

    if (p.isDbEncrypted()) {
      return dbTypeMap.get(p.getDbEncryptedType());
    }
    if (p.isLocalEncrypted()) {
      // scalar type potentially wrapping varbinary db type
      ScalarType<Object> scalarType = p.getScalarType();
      int jdbcType = scalarType.getJdbcType();
      return dbTypeMap.get(jdbcType);
    }

    // can be the logical JSON types (JSON, JSONB, JSONClob, JSONBlob, JSONVarchar)
    int dbType = p.getDbType(platformTypes);
    if (dbType == 0) {
      throw new RuntimeException("No scalarType defined for " + p.getFullBeanName());
    }
    return dbTypeMap.get(dbType);
  }

  /**
   * Create the draft table for a given table.
   */
  public void createDraft(MTable table, boolean draftable) {

    MTable draftTable = table.createDraftTable();
    draftTable.setPkName(primaryKeyName(draftTable.getName()));

    if (draftable) {
      // Add a FK from @Draftable live table back to it's draft table)
      List<MColumn> pkCols = table.primaryKeyColumns();
      if (pkCols.size() == 1) {
        // only doing this for single column PK at this stage
        MColumn pk = pkCols.get(0);
        pk.setReferences(draftTable.getName() + "." + pk.getName());
        pk.setForeignKeyName(foreignKeyConstraintName(table.getName(), pk.getName(), 0));
      }
    }

    int fkCount = 0;
    int ixCount = 0;
    int uqCount = 0;
    Collection<MColumn> cols = draftTable.allColumns();
    for (MColumn col : cols) {
      if (col.getForeignKeyName() != null) {
        // Note that we adjust the 'references' table later in a second pass
        // after we know all the tables that are 'draftable'
        //col.setReferences(refTable + "." + refColumn);
        col.setForeignKeyName(foreignKeyConstraintName(draftTable.getName(), col.getName(), ++fkCount));

        String[] indexCols = {col.getName()};
        col.setForeignKeyIndex(foreignKeyIndexName(draftTable.getName(), indexCols, ++ixCount));
      }
      // adjust the unique constraint names
      if (col.getUnique() != null) {
        col.setUnique(uniqueConstraintName(draftTable.getName(), col.getName(), ++uqCount));
      }
      if (col.getUniqueOneToOne() != null) {
        col.setUniqueOneToOne(uniqueConstraintName(draftTable.getName(), col.getName(), ++uqCount));
      }
    }

    addTable(draftTable);
  }

  /**
   * Return a builder to add foreign keys.
   */
  public FkeyBuilder fkeyBuilder(MTable destTable) {
    return new FkeyBuilder(this, destTable);
  }

  public static class FkeyBuilder {

    private final AtomicInteger count = new AtomicInteger();

    private final ModelBuildContext ctx;

    private final MTable destTable;

    private final String tableName;

    FkeyBuilder(ModelBuildContext ctx, MTable destTable) {
      this.ctx = ctx;
      this.destTable = destTable;
      this.tableName = destTable.getName();
    }

    /**
     * Add a foreign key based on the table join.
     */
    public FkeyBuilder addForeignKey(BeanDescriptor<?> desc, TableJoin tableJoin, boolean direction) {

      String baseTable = ctx.normaliseTable(desc.getBaseTable());
      String fkName = ctx.foreignKeyConstraintName(tableName, baseTable, count.incrementAndGet());
      String fkIndex = ctx.foreignKeyIndexName(tableName, baseTable, count.get());

      MCompoundForeignKey foreignKey = new MCompoundForeignKey(fkName, desc.getBaseTable(), fkIndex);

      for (TableJoinColumn column : tableJoin.columns()) {
        String localCol = direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
        String refCol = !direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
        foreignKey.addColumnPair(localCol, refCol);
      }

      destTable.addForeignKey(foreignKey);
      return this;
    }

  }
}
