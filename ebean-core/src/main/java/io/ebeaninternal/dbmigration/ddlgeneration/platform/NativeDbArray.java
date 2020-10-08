package io.ebeaninternal.dbmigration.ddlgeneration.platform;

/**
 * Helper for logical type conversion.
 */
class NativeDbArray {

  /**
   * Covert the 'logical' array type to a native one (for Postgres and Cockroach).
   */
  static String logicalToNative(String logicalArrayType) {
    int colonPos = logicalArrayType.lastIndexOf(']');
    if (colonPos == -1) {
      return logicalArrayType;
    } else {
      // trim of the fallback varchar length
      return logicalArrayType.substring(0, colonPos + 1);
    }
  }
}
