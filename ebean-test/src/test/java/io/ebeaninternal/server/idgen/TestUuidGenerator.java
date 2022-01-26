/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */
package io.ebeaninternal.server.idgen;

import io.ebean.config.dbplatform.PlatformIdGenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

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

  private File stateFile;

  @BeforeEach
  void beforeEach() throws IOException {
    stateFile = new File("target/" + UUID.randomUUID() + ".state");
  }

  @AfterEach
  void afterEach() {
    stateFile.delete();
  }

  private void writePropertyFile(String nodeId, String clockSeq, String timestamp) throws IOException {
    Properties prop = new Properties();
    prop.setProperty("nodeId", nodeId);
    prop.setProperty("clockSeq", clockSeq);
    prop.setProperty("timeStamp", timestamp);
    try (OutputStream os = new FileOutputStream(stateFile)) {
      prop.store(os, "ebean uuid state file");
    }
  }
  
  private Properties readPropertyFile() throws IOException {
    Properties prop = new Properties();
    try (InputStream is = new FileInputStream(stateFile)) {
      prop.load(is);
    }
    return prop;
  }
  /**
   * Test takes ~0.3 sec
   */
  @Test
  public void testUuidFixedMac() throws Exception {
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, "01-02-03-04-05-06");
    UUID uuid = gen.nextId(null);
    assertThat(uuid.node()).isEqualTo(0x010203040506L);
    Properties props = readPropertyFile();
    assertThat(props)
        .containsEntry("nodeId", "01-02-03-04-05-06")
        .containsEntry("clockSeq", String.valueOf(uuid.clockSequence()));
    assertThat(Long.parseLong(props.getProperty("timeStamp")))
        .isCloseTo(uuid.timestamp(), within(2_000_000L));
  }
  
  @Test
  public void testUuidInvalidMac() throws Exception {
    assertThatThrownBy(()-> UuidV1IdGenerator.getInstance(stateFile, "01-02-03-04-05"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("01-02-03-04-05 is invalid. Expected format: xx-xx-xx-xx-xx-xx");
    assertThatThrownBy(()-> UuidV1IdGenerator.getInstance(stateFile, "01-02-03-04-05-GG"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("01-02-03-04-05-gg is invalid.")
        .hasRootCauseInstanceOf(NumberFormatException.class)
        .hasRootCauseMessage("For input string: \"gg\"");
    assertThat(stateFile).doesNotExist();
  }

  @Test
  public void testUuidGenerate() throws Exception {
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, "generate");
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
    assertThat(stateFile).exists();
  }

  @Test
  public void testUuidRandom() throws Exception {
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, "random");
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
    assertThat(stateFile).doesNotExist();
  }

  @Test
  public void testUuidFile() throws Exception {
    writePropertyFile("12-34-56-78-90-AB", "4252", "0");
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, "generate");
    UUID uuid = gen.nextId(null);
    assertThat(uuid.node()).isEqualTo(0x1234567890ABL);
    assertThat(uuid.clockSequence()).isEqualTo(4252);
  }

  @Test
  public void testUuidFileInvalidNodeId() throws Exception {
    writePropertyFile("AB-CD-EF-GH-IJ-KL", "4252", "0");
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, "generate");
    // a error message will be printed
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
  }

  @Test
  public void testInvalidFileName() throws Exception {
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance("/", null);
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
  }

  @Test
  public void testInvalidStateFile() throws Exception {
    writePropertyFile("", "", "");
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, null);
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
  }
  
  @Test
  public void testMacChange() throws Exception {
    writePropertyFile("01-02-03-04-05", "1234", "1234");
    UuidV1IdGenerator gen = UuidV1IdGenerator.getInstance(stateFile, null);
    UUID uuid = gen.nextId(null);
    assertThat(uuid).isNotNull();
    // MAC must be updated with HW/ID
    assertThat(readPropertyFile()).doesNotContainEntry("nodeId", "01-02-03-04-05");
  }

  /**
   * Test takes ~0.3 sec
   */
  @Test
  public void testUuidV1SingleThread() throws Exception {
    testGenerator(1, 500_000, UuidV1IdGenerator.getInstance(stateFile, null));
  }

  /**
   * Test takes ~1.4 sec
   */
  @Test
  public void testUuidV1MultiThread() throws Exception {
    testGenerator(10, 50_000, UuidV1IdGenerator.getInstance(stateFile, null));
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
