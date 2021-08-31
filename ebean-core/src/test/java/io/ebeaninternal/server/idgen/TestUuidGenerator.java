/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */
package io.ebeaninternal.server.idgen;

import io.ebean.config.dbplatform.PlatformIdGenerator;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Run some simple tests for the UUID generator.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class TestUuidGenerator {

  /**
   * Worker to generate UUIDs
   *
   * @author Roland Praml, FOCONIS AG
   */
  public static class IdTest implements Runnable {
    private final Map<UUID, UUID> map;
    private final PlatformIdGenerator idGen;
    private final int ids;
    private final AtomicBoolean failFlag;

    public IdTest(PlatformIdGenerator idGen, int ids, Map<UUID, UUID> map, AtomicBoolean failFlag) {
      this.idGen = idGen;
      this.ids = ids;
      this.map = map;
      this.failFlag = failFlag;
    }

    @Override
    public void run() {
      //System.out.println("Start " + Thread.currentThread());
      for (int i = 0; i < ids; i++) {
        UUID uuid = (UUID) idGen.nextId(null);
        if (map != null && map.put(uuid, uuid) != null) {
          System.err.println("UUID already in set " + uuid);
          failFlag.set(true);
        }
      }
    }
  }

  /**
   * Test takes ~0.3 sec
   */
  @Test
  public void testUuidV1SingleThread() throws Exception {
    testGenerator(1, 500_000, UuidV1IdGenerator.getInstance("ebean-test-uuid.state"));
  }

  /**
   * Test takes ~1.4 sec
   */
  @Test
  public void testUuidV1MultiThread() throws Exception {
    testGenerator(10, 50_000, UuidV1IdGenerator.getInstance("ebean-test-uuid.state"));
  }

  /**
   * Test takes ~3.1 sec
   */
  @Test
  public void testUuidV1RndMultiThread() throws Exception {
    testGenerator(10, 50_000, UuidV1RndIdGenerator.INSTANCE);
  }

  /**
   * Test takes ~2.0 sec
   */
  @Test
  public void testUuidType1RndSingleThread() throws Exception {
    testGenerator(1, 500_000, UuidV1RndIdGenerator.INSTANCE);
  }

  /**
   * Test takes ~4.9 sec
   */
  @Test
  public void testUuidType4MultiThread() throws Exception {
    testGenerator(10, 50_000, UuidV4IdGenerator.INSTANCE);
  }

  /**
   * Test takes ~3.1 sec
   */
  @Test
  public void testUuidType4SingleThread() throws Exception {
    testGenerator(1, 500_000, UuidV4IdGenerator.INSTANCE);
  }

  private void testGenerator(int threadCount, int count, PlatformIdGenerator generator) throws Exception {
    System.out.println("Printing 5 consecutive IDs of " + generator);
    for (int i = 0; i < 5; i++) {
      System.out.println(generator.nextId(null));
    }

    AtomicBoolean failFlag = new AtomicBoolean();
    ConcurrentHashMap<UUID, UUID> map = new ConcurrentHashMap<>();
    Thread[] threads = new Thread[threadCount];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(new IdTest(generator, count, map, failFlag));
    }
    for (int i = 0; i < threads.length; i++) {
      threads[i].start();
    }
    for (int i = 0; i < threads.length; i++) {
      threads[i].join();
    }
    assertThat(failFlag.get()).isFalse();
    assertThat(map.size()).isEqualTo(threadCount * count);
  }
}
