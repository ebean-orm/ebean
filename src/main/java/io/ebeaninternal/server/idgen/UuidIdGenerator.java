package io.ebeaninternal.server.idgen;

import io.ebean.Transaction;
import io.ebean.config.dbplatform.PlatformIdGenerator;

import java.util.UUID;

/**
 * IdGenerator for java util UUID.
 */
public class UuidIdGenerator implements PlatformIdGenerator {

  public static final UuidIdGenerator INSTANCE = new UuidIdGenerator();

  /**
   * Return UUID from UUID.randomUUID();
   */
  @Override
  public Object nextId(Transaction t) {
    return UUID.randomUUID();
  }

  /**
   * Returns "uuid".
   */
  @Override
  public String getName() {
    return "uuid";
  }

  /**
   * Returns false.
   */
  @Override
  public boolean isDbSequence() {
    return false;
  }

  /**
   * Ignored for UUID as not required as a performance optimisation.
   */
  @Override
  public void preAllocateIds(int allocateSize) {
    // ignored
  }

}
