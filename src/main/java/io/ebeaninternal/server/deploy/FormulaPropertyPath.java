package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.query.STreeProperty;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.Types;

class FormulaPropertyPath {

  private static final String[] AGG_FUNCTIONS = {"count", "max", "min", "avg", "sum"};

  private static final String DISTINCT_ = "distinct ";

  private final BeanDescriptor<?> descriptor;

  private final String formula;

  private final String outerFunction;

  private final String internalExpression;

  private boolean countDistinct;

  private String cast;
  private String alias;

  FormulaPropertyPath(BeanDescriptor<?> descriptor, String formula) {

    this.descriptor = descriptor;
    this.formula = formula;

    int openBracket = formula.indexOf('(');
    int closeBracket = formula.lastIndexOf(')');
    if (openBracket == -1 || closeBracket == -1) {
      throw new IllegalStateException("Unable to parse formula [" + formula + "]");
    }
    outerFunction = formula.substring(0, openBracket).trim();
    internalExpression = trimDistinct(formula.substring(openBracket+1, closeBracket));

    if (closeBracket < formula.length() -1) {
      // ::CastType as foo
      String suffix = formula.substring(closeBracket+1).trim();
      parseSuffix(suffix);
    }
  }

  private void parseSuffix(String suffix) {
    String[] split = suffix.split(" ");
    if (split.length == 1) {
      if (split[0].startsWith("::")) {
        cast = split[0].substring(2);
      } else {
        alias = split[0];
      }

    } else if (split.length == 2) {
      cast = "as".equals(split[0]) ? null : split[0].substring(2);
      alias = split[1];

    } else if (split.length == 3) {
      cast = split[0].substring(2);
      alias = split[2];
    }
  }

  private String trimDistinct(String propertyName) {
    if (propertyName.startsWith(DISTINCT_)) {
      countDistinct = true;
      return propertyName.substring(DISTINCT_.length());
    } else {
      return propertyName;
    }
  }

  String outerFunction() {
    return outerFunction;
  }

  String internalExpression() {
    return internalExpression;
  }

  String cast() {
    return cast;
  }

  String alias() {
    return alias;
  }

  STreeProperty build() {

    DeployPropertyParser parser = descriptor.parser().setCatchFirst(true);

    String parsed = parser.parse(internalExpression);
    ElPropertyDeploy firstProp = parser.getFirstProp();

    ScalarType<?> scalarType;
    if (cast != null) {
      scalarType = descriptor.getScalarType(cast);
      if (scalarType == null) {
        throw new IllegalStateException("Unable to find scalarType for cast of ["+cast+"] on formula [" + formula + "] for type " + descriptor);
      }

    } else if (isCount()) {
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

    String logicalName = (alias == null) ? internalExpression : alias;
    BeanProperty targetProperty = descriptor._findBeanProperty(logicalName);
    String parsedAggregation = buildFormula(parsed);
    return new DynamicPropertyAggregationFormula(logicalName, scalarType, parsedAggregation, isAggregate(), targetProperty, alias);
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
