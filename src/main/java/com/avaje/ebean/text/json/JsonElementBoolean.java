package com.avaje.ebean.text.json;

/**
 * JSON boolean element.
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

public class JsonElementBoolean implements JsonElement {

  public static final JsonElementBoolean TRUE = new JsonElementBoolean(true);

  public static final JsonElementBoolean FALSE = new JsonElementBoolean(false);

  private final Boolean value;

  private JsonElementBoolean(Boolean value) {
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  public String toString() {
    return Boolean.toString(value);
  }

  public boolean isPrimitive() {
    return true;
  }

  public String toPrimitiveString() {
    return value.toString();
  }

  public Object eval(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on boolean");
    }
    return value;
  }

  public int evalInt(String exp) {
    return value ? 1 : 0;
  }

  public String evalString(String exp) {
    return toString();
  }

  public boolean evalBoolean(String exp) {
    return value;
  }

}
