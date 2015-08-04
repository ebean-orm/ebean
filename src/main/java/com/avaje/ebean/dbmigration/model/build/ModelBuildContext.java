package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;

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

  public ModelBuildContext(ModelContainer model) {
    this.model = model;
  }

  public void addTable(MTable table) {
    model.addTable(table);
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

    int dbType = p.getDbType();
    if (dbType == 0) {
//      ScalarType<Object> scalarType = p.getScalarType();
//      if (scalarType == null) {
        throw new RuntimeException("No scalarType for " + p.getFullBeanName());
//      }
//      dbType = scalarType.getJdbcType();
    }
    return dbTypeMap.get(dbType);
  }

}
