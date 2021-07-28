package io.ebean.bean;

/**
 * Interface to for mutable information in EntityBeanIntercept.
 */
public interface MutableHash {
  
  /**
   * Compares the given json to an internal value. Can be a MD5 hash or a plain JSON string. 
   * @return true if the value matches the hash.
   */
  boolean isEqualToJson(String json);

  /**
   * Compares the given object to an internal value. This is an optional method, but required for proper changelog/beanState support.
   * The implementation can serialize the object and compare it against the original json.
   */
  default boolean isEqualToObject(Object obj) {
    return true;
  }
  /**
   * Creates a new instance from the internal json string. This is an optional method.
   */
  default Object get() {
    return null;
  }
}
 