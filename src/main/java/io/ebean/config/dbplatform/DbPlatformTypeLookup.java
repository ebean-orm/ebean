package io.ebean.config.dbplatform;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper to reverse lookup a DbType given the name or JDBC int value.
 */
class DbPlatformTypeLookup {

  /**
   * A map to lookup the type by name.
   */
  private Map<String, DbType> nameLookup = new HashMap<>();

  /**
   * A map to lookup the type by JDBC int value.
   */
  private Map<Integer, DbType> idLookup = new HashMap<>();

  DbPlatformTypeLookup() {
    addAll();
  }

  /**
   * Return the DbType for the given name.
   */
  DbType byName(String name) {
    return nameLookup.get(name.trim().toUpperCase());
  }

  /**
   * Return the DbType for the given name.
   */
  DbType byId(int jdbcId) {
    return idLookup.get(jdbcId);
  }

  private void addAll() {
    // Extra mapping for Float and Varchar2
    add("FLOAT", DbType.REAL);
    add("VARCHAR2", DbType.VARCHAR);
    for (DbType type : DbType.values()) {
      add(type.name(), type);
    }
  }

  private void add(String name, DbType dbType) {
    nameLookup.put(name, dbType);
    idLookup.put(dbType.id(), dbType);
  }

}
