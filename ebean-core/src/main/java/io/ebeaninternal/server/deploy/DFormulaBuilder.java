package io.ebeaninternal.server.deploy;

import io.ebean.config.AggregateFormulaContext;
import io.ebeaninternal.api.FormulaBuilder;
import io.ebeaninternal.server.query.STreeProperty;

final class DFormulaBuilder implements FormulaBuilder {

  private final BeanDescriptor<?> descriptor;

  DFormulaBuilder(BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public STreeProperty create(AggregateFormulaContext context, String formula, String path) {
    return FormulaPropertyPath.create(descriptor, context, formula, path);
  }
}
