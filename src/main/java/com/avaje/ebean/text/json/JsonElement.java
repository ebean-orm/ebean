package com.avaje.ebean.text.json;

/**
 * Marker interface for all the Raw JSON types.
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
public interface JsonElement {

  /**
   * Return true if this is a JSON primitive type (null, boolean, number or
   * string).
   */
  public boolean isPrimitive();

  /**
   * Return the string value of this primitive JSON element.
   * <p>
   * This can not be used for JsonElementObject or JsonElementArray.
   * </p>
   */
  public String toPrimitiveString();

  public Object eval(String exp);

  public int evalInt(String exp);

  public String evalString(String exp);

  public boolean evalBoolean(String exp);

}
