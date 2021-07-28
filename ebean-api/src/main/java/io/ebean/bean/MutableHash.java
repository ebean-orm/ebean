package io.ebean.bean;

/**
 * Interface to for mutable information in EntityBeanIntercept.
 */
public interface MutableHash {

  /**
   * Compares the given json to an internal value. Can be a MD5 hash or a plain JSON string.
   *
   * @return true if the value matches the hash.
   */
  boolean isEqualToJson(String json);

  /**
   * Compares the given object to an internal value. Required for proper changelog/beanState support.
   * The implementation can serialize the object and compare it against the original json.
   */
  boolean isEqualToObject(Object obj);

  /**
   * Creates a new instance from the internal json string.
   * <p>
   * This is used to provide an original/old value for change logging / persist listeners.
   */
  default Object get() {
    return null;
  }
}
