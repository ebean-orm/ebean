package io.ebeaninternal.server.expression.platform;

/**
 * DB2 handling of platform specific expressions. ARRAY expressions not supported.
 */
final class Db2DbExpression extends BasicDbExpression {

  @Override
  public String concat(String property0, String separator, String property1, String suffix) {
    return concatOperator(property0, separator, property1, suffix);
  }

}
