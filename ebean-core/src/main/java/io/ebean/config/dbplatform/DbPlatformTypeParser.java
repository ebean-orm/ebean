package io.ebean.config.dbplatform;

/**
 * Parse raw column definitions into DbPlatformType like "decimal(18,6)" and "varchar(20) and "json".
 */
class DbPlatformTypeParser {

  /**
   * Parse the column definition and return the DbPlatformType.
   */
  static DbPlatformType parse(String columnDefinition) {

    columnDefinition = columnDefinition.trim();

    int openPos = columnDefinition.indexOf('(');
    if (openPos == -1) {
      return new DbPlatformType(columnDefinition, false);
    }
    int closePos = columnDefinition.indexOf(')', openPos);
    if (closePos == -1) {
      return new DbPlatformType(columnDefinition, false);
    }
    try {
      int commaPos = columnDefinition.indexOf(',', openPos);
      if (commaPos > -1) {
        String type = columnDefinition.substring(0, openPos);
        int scale = Integer.parseInt(columnDefinition.substring(openPos + 1, commaPos));
        int precision = Integer.parseInt(columnDefinition.substring(commaPos + 1, closePos));
        return new DbPlatformType(type, scale, precision);

      } else {
        String type = columnDefinition.substring(0, openPos);
        String strScale = columnDefinition.substring(openPos + 1, closePos);
        int scale = strScale.equalsIgnoreCase("max") ? Integer.MAX_VALUE : Integer.parseInt(strScale);
        return new DbPlatformType(type, scale);
      }
    } catch (RuntimeException e) {
      return new DbPlatformType(columnDefinition, false);
    }
  }
}
