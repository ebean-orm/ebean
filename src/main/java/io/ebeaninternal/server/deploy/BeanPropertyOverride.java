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
  private final int dbLength;
  private final int dbScale;
  private final String dbColumnDefn;

  BeanPropertyOverride(String dbColumn, boolean dbNullable, int dbLength, int  dbScale, String dbColumnDefn) {
    this.dbColumn = InternString.intern(dbColumn);
    this.dbNullable = dbNullable;
    this.dbLength = dbLength;
    this.dbScale = dbScale;
    this.dbColumnDefn = dbColumnDefn;
  }

  String getDbColumn() {
    return dbColumn;
  }

  boolean isDbNullable() {
    return dbNullable;
  }

  int getDbLength() {
    return dbLength;
  }

  int getDbScale() {
    return dbScale;
  }

  String getDbColumnDefn() {
    return dbColumnDefn;
  }

  String replace(String src, String srcDbColumn) {
    return StringHelper.replace(src, srcDbColumn, dbColumn);
  }
}
