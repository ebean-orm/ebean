package io.ebean.service;

import io.ebean.InTuples;

import java.util.List;

/**
 * Internal extension to InTuples.
 */
public interface SpiInTuples extends InTuples {

  /**
   * Return the properties of the tuples.
   */
  String[] properties();

  /**
   * Return all the tuple entries.
   */
  List<Object[]> entries();

}
