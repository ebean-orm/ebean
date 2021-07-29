package io.ebean.bean;

/**
 * Holds information on mutable values (like plain beans stored as json).
 * <p>
 * Used internally in EntityBeanIntercept for dirty detection on mutable values.
 * Typically dirty detection is based on a hash/checksum of json content or the
 * original json content itself.
 * <p>
 * Refer to the mapping options {@code @DbJson(dirtyDetection)} and {@code @DbJson(keepSource)}.
 */
public interface MutableValueInfo {

  /**
   * Compares the given json to an internal value. Can be a hash/checksum comparison
   * or a plain JSON string comparison (based on {@code @DbJson(keepSource)}).
   *
   * @return true if the value is considered unchanged (when comparing in json form).
   */
  boolean isEqualToJson(String json);

  /**
   * Compares the given object to an internal value.
   * <p>
   * This is used to support changelog/beanState. The implementation can serialize the
   * object into json form and compare it against the original json.
   */
  boolean isEqualToObject(Object obj);

  /**
   * Creates a new instance from the internal json string.
   * <p>
   * This is used to provide an original/old value for change logging / persist listeners.
   * This is only available for properties that have {@code @DbJson(keepSource=true)}.
   */
  default Object get() {
    return null;
  }
}
