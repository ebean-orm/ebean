package io.ebeaninternal.server.deploy;

import io.ebean.core.type.ScalarType;
import io.ebean.config.AggregateFormulaContext;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.query.STreeProperty;

import java.sql.Types;
import java.util.Set;

final class FormulaPropertyPath {

  private static final String DISTINCT_ = "distinct ";

  private final BeanDescriptor<?> descriptor;
  private final AggregateFormulaContext context;
  private final String formula;
  private final String outerFunction;
  private final String internalExpression;
  private final ElPropertyDeploy firstProp;
  private final String parsedAggregation;
  private final Set<String> includes;
  private boolean countDistinct;
  private String cast;
  private String alias;

  static STreeProperty create(BeanDescriptor<?> descriptor, AggregateFormulaContext context, String formula, String path) {
    return new FormulaPropertyPath(descriptor, context, formula, path).build();
  }

  FormulaPropertyPath(BeanDescriptor<?> descriptor, AggregateFormulaContext context, String formula, String path) {
    this.descriptor = descriptor;
    this.context = context;
    this.formula = formula;
    int openBracket = formula.indexOf('(');
    int closeBracket = formula.lastIndexOf(')');
    if (openBracket == -1 || closeBracket == -1) {
      throw new IllegalStateException("Unable to parse formula " + formula);
    }
    outerFunction = formula.substring(0, openBracket).trim();
    internalExpression = trimDistinct(formula.substring(openBracket + 1, closeBracket));

    if (closeBracket < formula.length() - 1) {
      // ::CastType as foo
      parseSuffix(formula.substring(closeBracket + 1).trim());
    }
    DeployPropertyParser parser = descriptor.parser().setCatchFirst(true);
    String parsed = parser.parse(internalExpression);
    if (path != null) {
      // fetch("machineStats", "sum(hours), sum(totalKms)")
      parsed = parsed.replace("${}", "${" + path + "}");
    }
    this.includes = parser.includes();
    this.parsedAggregation = buildFormula(parsed);
    this.firstProp = parser.firstProp();
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
    if (cast != null) {
      ScalarType<?> scalarType = descriptor.scalarType(cast);
      if (scalarType == null) {
        throw new IllegalStateException("Unable to find scalarType for cast of [" + cast + "] on formula [" + formula + "] for type " + descriptor);
      }
      return create(scalarType);
    }
    if (context.isCount(outerFunction)) {
      return create(descriptor.scalarType(Types.BIGINT));
    }
    if (context.isConcat(outerFunction)) {
      return create(descriptor.scalarType(Types.VARCHAR));
    }
    if (firstProp == null) {
      throw new IllegalStateException("unable to determine scalarType of formula [" + formula + "] for type " + descriptor + " - maybe use a cast like ::String ?");
    }
    // determine scalarType based on first property found by parser
    final BeanProperty property = firstProp.beanProperty();
    if (!property.isAssocId()) {
      return create(property.scalarType());
    } else {
      return createManyToOne(property);
    }
  }

  private DynamicPropertyAggregationFormula create(ScalarType<?> scalarType) {
    String logicalName = logicalName();
    return new DynamicPropertyAggregationFormula(logicalName, scalarType, parsedAggregation, isAggregate(), target(logicalName), alias);
  }

  @SuppressWarnings("rawtypes")
  private DynamicPropertyAggregationFormula createManyToOne(BeanProperty property) {
    String logicalName = logicalName();
    return new DynamicPropertyAggregationFormulaMTO((BeanPropertyAssocOne) property, logicalName, parsedAggregation, isAggregate(), target(logicalName), alias, includes);
  }

  private BeanProperty target(String logicalName) {
    return descriptor._findBeanProperty(logicalName);
  }

  private String logicalName() {
    return (alias == null) ? internalExpression : alias;
  }

  private boolean isAggregate() {
    return context.isAggregate(outerFunction);
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
