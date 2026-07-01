package io.ebeaninternal.api;

import io.ebean.config.AggregateFormulaContext;
import io.ebeaninternal.server.query.STreeProperty;

public interface FormulaBuilder {

  STreeProperty create(AggregateFormulaContext context, String formula, String path);

}
