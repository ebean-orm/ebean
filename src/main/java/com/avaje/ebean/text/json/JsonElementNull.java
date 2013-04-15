package com.avaje.ebean.text.json;

/**
 * JSON null element.
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
public class JsonElementNull implements JsonElement {

  public static final JsonElementNull NULL = new JsonElementNull();

  private JsonElementNull() {
  }

  public String getValue() {
    return "null";
  }

  public String toString() {
    return "json null";
  }

  public boolean isPrimitive() {
    return true;
  }

  public String toPrimitiveString() {
    return null;
  }

  public Object eval(String exp) {
    if (exp != null) {
      throw new IllegalArgumentException("expression [" + exp + "] not allowed on null");
    }
    return null;
  }

  public int evalInt(String exp) {
    return 0;
  }

  public String evalString(String exp) {
    return null;
  }

  public boolean evalBoolean(String exp) {
    return false;
  }

}
