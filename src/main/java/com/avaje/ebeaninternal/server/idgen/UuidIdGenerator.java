package com.avaje.ebeaninternal.server.idgen;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.dbplatform.PlatformIdGenerator;

import java.util.UUID;

/**
 * IdGenerator for java util UUID.
 */
public class UuidIdGenerator implements PlatformIdGenerator {

  public static UuidIdGenerator INSTANCE = new UuidIdGenerator();

  /**
   * Return UUID from UUID.randomUUID();
   */
  public Object nextId(Transaction t) {
    return UUID.randomUUID();
  }

  /**
   * Returns "uuid".
   */
  public String getName() {
    return "uuid";
  }

  /**
   * Returns false.
   */
  public boolean isDbSequence() {
    return false;
  }

  /**
   * Ignored for UUID as not required as a performance optimisation.
   */
  public void preAllocateIds(int allocateSize) {
    // ignored
  }

}
