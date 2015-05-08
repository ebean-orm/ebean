package com.avaje.ebean.bean;

/**
 * Used to specify a listener to be notified when a BeanCollection is first
 * used.
 * <p>
 * To use this you can set a BeanCollectionTouched onto a BeanCollection before
 * it has been used. When the BeanCollection is first used by the client code
 * then the BeanCollectionTouched is notified. It can only be notified once.
 * </p>
 * 
 * @author rbygrave
 */
public interface BeanCollectionTouched {

  /**
   * Notify the listener that the bean collection has been used.
   */
  void notifyTouched(BeanCollection<?> c);
}
