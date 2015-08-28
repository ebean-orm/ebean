package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.ScalarType;

/**
 * The context used during DDL generation.
 */
public class ModelBuildContext {

  /**
   * Use platform agnostic logical types. These types are converted to
   * platform specific types in the DDL generation.
   */
  private final DbTypeMap dbTypeMap = DbTypeMap.logicalTypes();

  private final ModelContainer model;

  private final DbConstraintNaming constraintNaming;

  private final DbConstraintNaming.MaxLength maxLength;

  public ModelBuildContext(ModelContainer model, DbConstraintNaming naming, DbConstraintNaming.MaxLength maxLength) {
    this.model = model;
    this.constraintNaming = naming;
    this.maxLength = maxLength;
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

  public void addTable(MTable table) {
    model.addTable(table);
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
  public DbTypeMap getDbTypeMap() {
    return dbTypeMap;
  }


  public String getColumnDefn(BeanProperty p) {
    DbType dbType = getDbType(p);
    if (dbType == null) {
      throw new IllegalStateException("Unknown DbType mapping for " + p.getFullBeanName());
    }
    return p.renderDbType(dbType);
  }

  private DbType getDbType(BeanProperty p) {

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
    int dbType = p.getDbType();
    if (dbType == 0) {
      throw new RuntimeException("No scalarType defined for " + p.getFullBeanName());
    }
    return dbTypeMap.get(dbType);
  }

}
