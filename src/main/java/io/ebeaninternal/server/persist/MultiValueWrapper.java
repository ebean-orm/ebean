package io.ebeaninternal.server.persist;
/**
 * Wraps the multi values that are used for "property in (...)" queries 
 * @author Roland Praml, FOCONIS AG
 */
public class MultiValueWrapper {
  private final Object[] values;
  private final Class<?> type;
  
  public MultiValueWrapper(Object[] values) {
    this.values = values;
    this.type = values[0].getClass();
  }
  
  public Object[] getValues() {
    return values;
  }
  
  public Class<?> getType() {
    return type;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Array[" + values.length + "]={");
    for (Object value : values) {
      sb.append(value).append(',');
      if (sb.length() > 50) {
        sb.append("...}");
        return sb.toString();
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append('}');
    return sb.toString();
  }
}
