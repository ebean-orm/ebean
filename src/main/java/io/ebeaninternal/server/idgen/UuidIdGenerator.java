package io.ebeaninternal.server.idgen;

import io.ebean.Transaction;
import io.ebean.config.dbplatform.PlatformIdGenerator;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IdGenerator for java util UUID.
 */
public class UuidIdGenerator implements PlatformIdGenerator {

  public static final UuidIdGenerator INSTANCE = new UuidIdGenerator();

  // RFC 4.1.1.  Variant: must set bits 10xx
  private static AtomicInteger clockSeq = new AtomicInteger(0x8000 | (int)(Math.random() * 0x3FFF) );
  private static final SecureRandom numberGenerator = new SecureRandom();

  private static AtomicLong lastTime = new AtomicLong(Long.MIN_VALUE);

  /**
   * Return UUID from UUID.randomUUID();
   */
  @Override
  public Object nextId(Transaction t) {

    long timeMillis = (System.currentTimeMillis() * 10000) + 0x01B21DD213814000L; // 00:00:00.00, 15 October 1582.
    while (true) {
      long current = lastTime.get();
      if (timeMillis < current - 10000*20) {
        // Time shift of 20 seconds detected
        synchronized (this) {
          if (lastTime.compareAndSet(current, timeMillis)) {
            clockSeq.incrementAndGet();
          }
        }
      } if (timeMillis > current && lastTime.compareAndSet(current, timeMillis)) {
        break;
      } else if (lastTime.compareAndSet(current, current + 1)) {
        timeMillis = current + 1;
        break;
      }
    }


    long msb = timeMillis << 32;   // time low
    msb |= (timeMillis & 0xFFFF00000000L) >> 16; // time mid
    msb |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // time hi and version 1

    // RFC 4.5 use random portion for node
    byte[] randomBytes = new byte[6];
    numberGenerator.nextBytes(randomBytes);
    randomBytes[0] |= 0x01; // set multicast bit

    long lsb = 0;
    for (int i=0; i<6; i++) {
      lsb = (lsb << 8) | (randomBytes[i] & 0xff);
    }
    lsb |= (long)clockSeq.get() << 48;

    return new UUID(msb, lsb);
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
