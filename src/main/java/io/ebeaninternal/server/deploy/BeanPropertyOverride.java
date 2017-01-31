package io.ebeaninternal.server.deploy;

import io.ebean.util.StringHelper;
import io.ebeaninternal.server.core.InternString;

/**
 * Used hold meta data when a bean property is overridden.
 * <p>
 * Typically this is for Embedded Beans.
 * </p>
 */
public class BeanPropertyOverride {

  private final String dbColumn;

  public BeanPropertyOverride(String dbColumn) {
    this.dbColumn = InternString.intern(dbColumn);
  }

  public String getDbColumn() {
    return dbColumn;
  }

  public String replace(String src, String srcDbColumn) {
    return StringHelper.replaceString(src, srcDbColumn, dbColumn);
  }
}
