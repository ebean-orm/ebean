package io.ebeaninternal.server.deploy;

/**
 * Collects (List/Set/Map) of elements.
 */
public interface ElementCollector {

  /**
   * Add an element.
   */
  void addElement(Object element);

  /**
   * Return the populated collection/map.
   */
  Object collection();
}
