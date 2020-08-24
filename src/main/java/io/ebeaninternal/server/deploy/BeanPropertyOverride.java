package io.ebeaninternal.server.deploy;

import io.ebean.util.StringHelper;
import io.ebeaninternal.server.core.InternString;

/**
 * Used hold meta data when a bean property is overridden.
 * <p>
 * Typically this is for Embedded Beans.
 * </p>
 */
class BeanPropertyOverride {

  private final String dbColumn;
  private final boolean dbNullable;

  BeanPropertyOverride(String dbColumn, boolean dbNullable) {
    this.dbColumn = InternString.intern(dbColumn);
    this.dbNullable = dbNullable;
  }

  String getDbColumn() {
    return dbColumn;
  }

  boolean isDbNullable() {
    return dbNullable;
  }

  String replace(String src, String srcDbColumn) {
    return StringHelper.replaceString(src, srcDbColumn, dbColumn);
  }
}
