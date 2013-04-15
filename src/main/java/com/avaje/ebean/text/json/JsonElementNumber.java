package com.avaje.ebean.text.json;

/**
 * JSON number element.
 * <p>
 * You will only use the JsonElements when you register a JsonReadBeanVisitor.
 * The JSON elements that are not mapped to a bean property are made available
 * to the JsonReadBeanVisitor.
 * </p>
 * 
 * @see JsonReadBeanVisitor
 * 
 * @author rbygrave
 */
public class JsonElementNumber implements JsonElement {

  private final String value;

  public JsonElementNumber(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String toString() {
    return value;
  }

  public boolean isPrimitive() {
    return true;
  }

  public String toPrimitiveString() {
    return value;
  }

  public Object eval(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on number");
    }
    return Double.parseDouble(value);
  }

  public int evalInt(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on number");
    }
    return Integer.parseInt(value);
  }

  public String evalString(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on number");
    }
    return value;
  }

  public boolean evalBoolean(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on number");
    }
    return Boolean.parseBoolean(value);
  }

}
