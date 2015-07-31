package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.Transaction;

/**
 * Generates unique id's for objects. This occurs prior to the actual insert.
 * <p>
 * Note that many databases have sequences or auto increment features. These can
 * be used rather than an IdGenerator and are different in that they occur
 * during an insert. IdGenerator is used to generate an id <em>BEFORE</em> the
 * actual insert.
 * </p>
 */
public interface IdGenerator {

  /**
   * The name of the default UUID generator.
   */
  String AUTO_UUID = "auto.uuid";

  /**
   * Return the name of the IdGenerator. For sequences this is the sequence
   * name.
   */
  String getName();

  /**
   * Return true if this is a DB sequence.
   */
  boolean isDbSequence();

  /**
   * return the next unique identity value.
   * <p>
   * Note the transaction passed in can be null.
   * </p>
   */
  Object nextId(Transaction transaction);

  /**
   * Is called prior to inserting OneToMany's as an indication that a number of
   * beans are likely to need id's shortly.
   * <p>
   * Can be used as a performance optimisation to prefetch a number of Id's.
   * Especially when the allocateSize is very large.
   * </p>
   */
  void preAllocateIds(int allocateSize);

}
