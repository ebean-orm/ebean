package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Logical array type to ClickHouse array type conversion.
 */
class ClickHouseDbArray {

  private static final Map<String, String> mapping = new HashMap<>();
  static {
    mapping.put("uuid[]", "Array(UUID)");
    mapping.put("varchar[]", "Array(String)");
    mapping.put("integer[]", "Array(UInt32)");
    mapping.put("bigint[]", "Array(UInt64)");
    mapping.put("float[]", "Array(Float32)");
  }

  /**
   * Covert the 'logical' array type to a native one (for Postgres and Cockroach).
   */
  static String logicalToNative(String logicalArrayType) {
    int colonPos = logicalArrayType.indexOf(':');
    int bracketPos = logicalArrayType.indexOf('(');
    if (bracketPos > -1 && (bracketPos < colonPos || colonPos == -1)) {
      colonPos = bracketPos;
    }
    if (colonPos > -1) {
      logicalArrayType = logicalArrayType.substring(0, colonPos);
    }
    String clickHouseType = mapping.get(logicalArrayType);
    if (clickHouseType == null) {
      throw new IllegalStateException("No mapping for logical array type " + logicalArrayType);
    }
    return clickHouseType;
  }
}
