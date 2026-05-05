package io.ebean.config;

import java.util.Set;

final class AggregateFormulaContextBuilder implements AggregateFormulaContext.Builder {

  private Set<String> aggFunctions = Set.of("count", "max", "min", "avg", "sum", "group_concat", "string_agg", "listagg");
  private Set<String> concat = Set.of("concat", "group_concat", "string_agg", "listagg");
  private Set<String> count = Set.of("count");

  @Override
  public AggregateFormulaContext.Builder aggregateFunctions(Set<String> agg) {
    this.aggFunctions = agg;
    return this;
  }

  @Override
  public AggregateFormulaContext.Builder concatFunctions(Set<String> concat) {
    this.concat = concat;
    return this;
  }

  @Override
  public AggregateFormulaContext.Builder countFunctions(Set<String> count) {
    this.count = count;
    return this;
  }

  @Override
  public AggregateFormulaContext build() {
    return new FormulaContext(aggFunctions, concat, count);
  }

  private static final class FormulaContext implements AggregateFormulaContext {

    private final Set<String> aggFunctions;
    private final Set<String> concat;
    private final Set<String> count;

    private FormulaContext(Set<String> aggFunctions, Set<String> concat, Set<String> count) {
      this.aggFunctions = aggFunctions;
      this.concat = concat;
      this.count = count;
    }

    @Override
    public boolean isAggregate(String outerFunction) {
      return aggFunctions.contains(outerFunction);
    }

    @Override
    public boolean isCount(String outerFunction) {
      return count.contains(outerFunction);
    }

    @Override
    public boolean isConcat(String outerFunction) {
      return concat.contains(outerFunction);
    }
  }
}
