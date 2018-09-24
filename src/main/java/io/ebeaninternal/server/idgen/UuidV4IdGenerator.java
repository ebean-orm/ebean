package io.ebeaninternal.server.idgen;

import io.ebean.Transaction;
import io.ebean.config.dbplatform.PlatformIdGenerator;

import java.util.UUID;

/**
 * IdGenerator for java util UUID.
 *
 * This generator generates a 60bit random UUID according to
 * https://tools.ietf.org/html/rfc4122.html#section-4.4
 * Use this generator if you want truly random UUIDs
 */
public class UuidV4IdGenerator implements PlatformIdGenerator {

  public static final UuidV4IdGenerator INSTANCE = new UuidV4IdGenerator();

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
