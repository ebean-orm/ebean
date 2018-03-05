package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlTreeProperty;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FormulaPropertyPath {

  private static final Pattern pattern = Pattern.compile("(max|min|avg|count)\\((.*)\\)");

  private static final String DISTINCT_ = "distinct ";

  private final String aggType;

  private final String baseName;

  private boolean countDistinct;

  FormulaPropertyPath(String propName) {

    Matcher matcher = pattern.matcher(propName);
    if (matcher.find()) {
      aggType = matcher.group(1);
      baseName = trimDistinct(matcher.group(2));

    } else {
      aggType = null;
      baseName = null;
    }
  }

  private String trimDistinct(String propertyName) {
    if (propertyName.startsWith(DISTINCT_)){
      countDistinct = true;
      return propertyName.substring(DISTINCT_.length());
    } else {
      return propertyName;
    }
  }

  boolean isFormula() {
    return aggType != null;
  }

  String aggType() {
    return aggType;
  }

  String basePropertyName() {
    return baseName;
  }

  /**
   * Create a bean property dynamically for the formula in the select clause.
   */
  SqlTreeProperty formulaProperty(BeanProperty base) {

    String parsedAggregation = buildFormula(base);
    String name = logicalName();

    ScalarType<?> scalarType = base.getScalarType();
    if (isCount()) {
      // count maps to Long / BIGINT
      scalarType = base.getBeanDescriptor().getScalarType(Types.BIGINT);
    }

    return new DynamicPropertyAggregationFormula(name, scalarType, parsedAggregation, null);
  }

  private String buildFormula(BeanProperty base) {
    if (countDistinct) {
      return "count(distinct ${}"+base.getDbColumn()+")";
    } else {
      return aggType+"(${}"+base.getDbColumn()+")";
    }
  }

  private boolean isCount() {
    return aggType.equals("count");
  }

  private String logicalName() {
    return aggType+Character.toUpperCase(baseName.charAt(0))+baseName.substring(1);
  }

}
