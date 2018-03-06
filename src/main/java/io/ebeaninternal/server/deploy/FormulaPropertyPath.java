package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.query.SqlTreeProperty;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FormulaPropertyPath {

  private static final String[] AGG_FUNCTIONS = {"count", "max", "min", "avg"};

  private static final Pattern pattern = Pattern.compile("([a-zA-Z]*)\\((.*)\\)");

  private static final String DISTINCT_ = "distinct ";

  private final BeanDescriptor<?> descriptor;

  private final String formula;

  private final String outerFunction;

  private final String internalExpression;

  private boolean countDistinct;

  FormulaPropertyPath(BeanDescriptor<?> descriptor, String formula) {

    this.descriptor = descriptor;
    this.formula = formula;

    Matcher matcher = pattern.matcher(formula);
    if (!matcher.find()) {
      throw new IllegalStateException("Unable to parse formula [" + formula + "]");
    }
    //int groupCount = matcher.groupCount();
    outerFunction = matcher.group(1);
    internalExpression = trimDistinct(matcher.group(2));
  }

  private String trimDistinct(String propertyName) {
    if (propertyName.startsWith(DISTINCT_)) {
      countDistinct = true;
      return propertyName.substring(DISTINCT_.length());
    } else {
      return propertyName;
    }
  }

  String aggType() {
    return outerFunction;
  }

  String basePropertyName() {
    return internalExpression;
  }


  SqlTreeProperty build() {

    DeployPropertyParser parser = descriptor.parser().setCatchFirst(true);

    String parsed = parser.parse(internalExpression);
    ElPropertyDeploy firstProp = parser.getFirstProp();

    ScalarType<?> scalarType;
    if (isCount()) {
      scalarType = descriptor.getScalarType(Types.BIGINT);

    } else if (isConcat()) {
      scalarType = descriptor.getScalarType(Types.VARCHAR);

    } else {
      // determine scalarType based on first property found by parser
      if (firstProp != null) {
        scalarType = firstProp.getBeanProperty().getScalarType();
      } else {
        throw new IllegalStateException("unable to determine scalarType of formula [" + formula + "] for type " + descriptor + " - maybe use a cast like ::String ?");
      }
    }

    String parsedAggregation = buildFormula(parsed);
    return new DynamicPropertyAggregationFormula(formula, scalarType, parsedAggregation, isAggregate(), null);
  }

  private boolean isAggregate() {
    for (String aggFunction : AGG_FUNCTIONS) {
      if (aggFunction.equals(outerFunction)) {
        return true;
      }
    }
    return false;
  }

  private String buildFormula(String parsed) {
    if (countDistinct) {
      return "count(distinct " + parsed + ")";
    } else {
      return outerFunction + "(" + parsed + ")";
    }
  }

  private boolean isCount() {
    return outerFunction.equals("count");
  }

  private boolean isConcat() {
    return outerFunction.equals("concat");
  }

}
