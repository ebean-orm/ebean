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
public class ElPropertyChainBuilder {

  private final String expression;

  private final List<ElPropertyValue> chain = new ArrayList<>();

  private boolean embedded;

  private boolean containsMany;

  /**
   * Create with the original expression.
   */
  public ElPropertyChainBuilder(boolean embedded, String expression) {
    this.embedded = embedded;
    this.expression = expression;
  }

  public boolean isContainsMany() {
    return containsMany;
  }

  public void setContainsMany() {
    this.containsMany = true;
  }

  public String getExpression() {
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
    return this;
  }

  /**
   * Build the immutable ElGetChain from the build information.
   */
  public ElPropertyChain build() {
    return new ElPropertyChain(containsMany, embedded, expression, chain.toArray(new ElPropertyValue[chain.size()]));
  }

  /**
   * Permits to set whole chain as embedded when the leaf is embedded
   * @param embedded
     */
  public void setEmbedded(boolean embedded) {
    this.embedded = embedded;
  }
}
