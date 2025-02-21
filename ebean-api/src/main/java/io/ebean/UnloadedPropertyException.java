package io.ebean;

/**
 * Thrown when trying to access a property that isn't loaded on a read only entity.
 * <p>
 * On a normal mutable entity accessing the property would invoke lazy loading. On
 * a read only entity with lazy loading disabled, accessing an unloaded property
 * throws this UnloadedPropertyException instead.
 */
public class UnloadedPropertyException extends UnmodifiableEntityException {

  /**
   * Create specifying the property that was being accessed.
   */
  public UnloadedPropertyException(String message) {
    super(message);
  }
}
