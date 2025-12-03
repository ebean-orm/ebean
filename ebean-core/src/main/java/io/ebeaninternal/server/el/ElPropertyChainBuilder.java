package io.ebeaninternal.server.el;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility object used to build a ElPropertyChain.
 * <p>
 * Builds a ElPropertyChain based on a chain of properties with dot separators.
 * </p>
 * <p>
 * This can navigate an object graph based on dot notation such as
 * order.customer.name.
 * </p>
 */
public final class ElPropertyChainBuilder {

  private final String expression;
  private final List<ElPropertyValue> chain = new ArrayList<>();
  private boolean containsMany = false;

  /**
   * Create with the original expression.
   */
  public ElPropertyChainBuilder(String expression) {
    this.expression = expression;
  }

  public boolean isContainsMany() {
    return containsMany;
  }

  public String expression() {
    return expression;
  }

  /**
   * Add a ElGetValue element to the chain.
   */
  public ElPropertyChainBuilder add(ElPropertyValue element) {
    if (element == null) {
      throw new NullPointerException("element null in expression " + expression);
    }
    chain.add(element);
    if (element.containsMany()) {
      containsMany = true;
    }
    return this;
  }

  /**
   * Build the immutable ElGetChain from the build information.
   */
  public ElPropertyChain build() {
    return new ElPropertyChain(expression, containsMany, chain.toArray(new ElPropertyValue[0]));
  }
}
