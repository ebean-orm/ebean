package io.ebeaninternal.dbmigration.model.build;

import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import io.ebeaninternal.dbmigration.model.MCompoundForeignKey;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.TableJoinColumn;
import io.ebeaninternal.server.type.ScalarType;

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

  private final DbConstraintNaming constraintNaming;

  private final DbConstraintNaming.MaxLength maxLength;

  private final boolean platformTypes;

  public ModelBuildContext(ModelContainer model, DbConstraintNaming naming, DbConstraintNaming.MaxLength maxLength, boolean platformTypes) {
    this.model = model;
    this.constraintNaming = naming;
    this.maxLength = maxLength;
    this.platformTypes = platformTypes;
  }

  public String normaliseTable(String baseTable) {
    return constraintNaming.normaliseTable(baseTable);
  }

  public String primaryKeyName(String tableName) {
    return maxLength(constraintNaming.primaryKeyName(tableName), 0);
  }

  public String foreignKeyConstraintName(String tableName, String columnName, int foreignKeyCount) {
    return maxLength(constraintNaming.foreignKeyConstraintName(tableName, columnName), foreignKeyCount);
  }

  public String foreignKeyIndexName(String tableName, String[] columns, int indexCount) {
    return maxLength(constraintNaming.foreignKeyIndexName(tableName, columns), indexCount);
  }

  public String foreignKeyIndexName(String tableName, String column, int indexCount) {
    return maxLength(constraintNaming.foreignKeyIndexName(tableName, column), indexCount);
  }

  public String indexName(String tableName, String column, int indexCount) {
    return maxLength(constraintNaming.indexName(tableName, column), indexCount);
  }

  public String indexName(String tableName, String[] columns, int indexCount) {
    return maxLength(constraintNaming.indexName(tableName, columns), indexCount);
  }

  public String uniqueConstraintName(String tableName, String columnName, int indexCount) {
    return maxLength(constraintNaming.uniqueConstraintName(tableName, columnName), indexCount);
  }

  public String uniqueConstraintName(String tableName, String[] columnNames, int indexCount) {
    return maxLength(constraintNaming.uniqueConstraintName(tableName, columnNames), indexCount);
  }

  public String checkConstraintName(String tableName, String columnName, int checkCount) {
    return maxLength(constraintNaming.checkConstraintName(tableName, columnName), checkCount);
  }

  public MTable addTable(MTable table) {
    return model.addTable(table);
  }

  public void addIndex(String indexName, String tableName, String columnName) {
    model.addIndex(indexName, tableName, columnName);
  }

  public void addIndex(String indexName, String tableName, String[] columnNames) {
    model.addIndex(indexName, tableName, columnNames);
  }

  private String maxLength(String constraintName, int indexCount) {
    return maxLength.maxLength(constraintName, indexCount);
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
    FkeyBuilder addForeignKey(BeanDescriptor<?> desc, TableJoin tableJoin, boolean direction) {

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
