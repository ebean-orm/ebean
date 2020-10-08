package io.ebeaninternal.server.expression;

/**
 * Abstract expression that helps with named parameter use.
 */
public abstract class AbstractValueExpression extends AbstractExpression {

  protected final Object bindValue;

  /**
   * Construct with property name and potential named parameter.
   */
  protected AbstractValueExpression(String propName, Object bindValue) {
    super(propName);
    this.bindValue = bindValue;
  }

  /**
   * Return the bind value taking into account named parameters.
   */
  protected Object value() {
    return NamedParamHelp.value(bindValue);
  }

  /**
   * Return the String bind value taking into account named parameters.
   */
  protected String strValue() {
    return NamedParamHelp.valueAsString(bindValue);
  }

}
