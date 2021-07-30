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
   * Compares the given json returning null if deemed unchanged or returning
   * the MutableValueNext to use if deemed dirty/changed.
   * <p>
   * Returning MutableValueNext allows an implementation based on hash/checksum
   * to only perform that computation once.
   *
   * @return Null if deemed unchanged or the MutableValueNext if deemed changed.
   */
  MutableValueNext nextDirty(String json);

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
