package com.avaje.ebean.config.dbplatform;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DB Column default values mapping to database platform specific literals.
 */
public class DbDefaultValue {

  /**
   * The key for FALSE.
   */
  public static final String FALSE = "false";

  /**
   * The key for TRUE.
   */
  public static final String TRUE = "true";

  /**
   * The key for the NOW / current timestamp.
   */
  public static final String NOW = "now";

  protected Map<String,String> map = new LinkedHashMap<String,String>();

  /**
   * Set the DB now function.
   */
  public void setNow(String dbFunction) {
    put(NOW, dbFunction);
  }

  /**
   * Set the DB false literal.
   */
  public void setFalse(String dbFalseLiteral) {
    put(FALSE, dbFalseLiteral);
  }

  /**
   * Set the DB true literal.
   */
  public void setTrue(String dbTrueLiteral) {
    put(TRUE, dbTrueLiteral);
  }

  /**
   * Add an translation entry.
   */
  public void put(String dbLiteral, String dbTranslated) {
    map.put(dbLiteral, dbTranslated);
  }

  /**
   * Convert the DB default literal to platform specific type or function.
   * <p>
   * This is intended for the DB column default clause in DDL.
   * </p>
   */
  public String convert(String dbDefaultLiteral) {
    if (dbDefaultLiteral == null) {
      return null;
    }
    String val = map.get(dbDefaultLiteral);
    return val != null ? val : dbDefaultLiteral;
  }

}
