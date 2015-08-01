package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;

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
