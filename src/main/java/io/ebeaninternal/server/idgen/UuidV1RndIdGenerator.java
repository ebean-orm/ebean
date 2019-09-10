package io.ebeaninternal.server.idgen;

import io.ebean.Transaction;
import io.ebean.config.dbplatform.PlatformIdGenerator;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IdGenerator for (pseudo) type 1 UUIDs.
 *
 * This implementation generates a type 1 UUID according to
 * https://tools.ietf.org/html/rfc4122.html#section-4.2
 * but has no persistence storage. It generates a new random 47 bit node ID with every
 * UUID.
 *
 * Use this, if you want randomness in your UUIDs but want to take advantage of index
 * optimizations of the database. It may be good with AUTO_BINARY_OPTIMIZED and MySql.
 *
 * See: https://www.percona.com/blog/2014/12/19/store-uuid-optimized-way/
 */
public class UuidV1RndIdGenerator implements PlatformIdGenerator {

  protected static final Logger logger = LoggerFactory.getLogger("io.ebean.IDGEN");

  // UUID epoch 1582-10-15 00:00:00 and the Unix epoch 1970-01-01 00:00:00.
  protected static final long UUID_EPOCH_OFFSET = 0x01B21DD213814000L;

  // the resolution is 100ns
  protected static final long MILLIS_TO_UUID = 10000;

  public static final UuidV1RndIdGenerator INSTANCE = new UuidV1RndIdGenerator();

  protected final AtomicInteger clockSeq = new AtomicInteger((int) (Math.random() * 0x3FFF));

  private final SecureRandom numberGenerator = new SecureRandom();

  protected AtomicLong timeStamp = new AtomicLong(currentUuidTime());

  private AtomicLong nanoToMilliOffset = new AtomicLong(currentUuidTime());


  /**
   * Returns the uuid epoch.
   *
   * This is the number of 100ns intervals since 1582-10-15 00:00:00
   */
  private static long currentUuidTime() {
    return (System.currentTimeMillis() * MILLIS_TO_UUID) + UUID_EPOCH_OFFSET;
  }

  public UuidV1RndIdGenerator() {
    computeNanoOffset();
  }

  /**
   * Computes the internal offset between System.nanoTime() and System.currentTimeMillis().
   */
  protected void computeNanoOffset() {
    long currentTime = currentUuidTime();
    long fromNanos = System.nanoTime() / 100L + nanoToMilliOffset.get();
    long offset =  currentTime - fromNanos;
    if (Math.abs(offset) > MILLIS_TO_UUID * 1000  ) {
      nanoToMilliOffset.addAndGet(offset);
    }
  }

  /**
   * Method to overwrite to save the state in a non volatile place.
   */
  protected void saveState() {

  }

  /**
   * returns a random 47 bit value according to https://tools.ietf.org/html/rfc4122.html#section-4.5
   */
  protected byte[] getNodeIdBytes() {
    byte[] idBytes = new byte[6];
    numberGenerator.nextBytes(idBytes);
    idBytes[0] |= 0x01; // set multicast bit.
    return idBytes;
  }

  /**
   * Return UUID from UUID.randomUUID();
   */
  @Override
  public UUID nextId(Transaction t) {
    long current = System.nanoTime() / 100L + nanoToMilliOffset.get();

    long delta;
    int seq;
    do {
      seq = clockSeq.get();
      while (true) {
        long last = timeStamp.get();

        delta = current - last;
        if (delta < -10000 * 20000) {
          logger.info("Clock skew of {} ms detected", delta / -10000);
          // The clock was adjusted back about 2 seconds, or we were generating a lot of ids too fast
          // if so, we try to set the current as last and also increment the clockSeq.
          synchronized (this) {
            if (clockSeq.compareAndSet(seq, seq + 1)) {
              timeStamp.set(current);
              saveState();
              computeNanoOffset();
            } else {
              continue;
            }
          }
        }
        // If current is in the future (most of the cases) try to set it.
        if (delta > 0 && timeStamp.compareAndSet(last, current)) {
          break;
        } else if (timeStamp.compareAndSet(last, last + 1)) {
          // here we go, if we pull IDs too fast.
          current = last + 1;
          break;
        }
      }
      // verify, if this timestamp is for this clock sequence
    } while (seq != clockSeq.get());

    // save state every 60 seconds
    if (delta > 60000 * 10000) {
      saveState();
      computeNanoOffset();
    }

    long msb = current << 32; // time low
    msb |= (current & 0xFFFF00000000L) >> 16; // time mid
    msb |= 0x1000 | ((current >> 48) & 0x0FFF); // time hi and version 1

    byte[] idBytes = getNodeIdBytes();

    long lsb = 0;
    for (int i = 0; i < 6; i++) {
      lsb = (lsb << 8) | (idBytes[i] & 0xff);
    }

    // RFC 4.1.1. Variant: is set with bits 10xx
    seq = seq & 0x3FFF | 0x8000;
    lsb |= (long) seq << 48;

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
