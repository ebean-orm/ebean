package io.ebean;

/**
 * Thrown when trying to access a property that isn't loaded on an entity
 * that is unmodifiable or has disabled lazy loading.
 * <p>
 * On a normal mutable entity accessing the property would invoke lazy loading. On
 * a unmodifiable entity with lazy loading disabled, accessing an unloaded property
 * throws this LazyInitialisationException instead.
 */
public class LazyInitialisationException extends UnmodifiableEntityException {

  /**
   * Create specifying the property that was being accessed.
   */
  public LazyInitialisationException(String message) {
    super(message);
  }
}
