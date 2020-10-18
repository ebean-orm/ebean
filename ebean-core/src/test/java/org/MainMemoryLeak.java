package org;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.annotation.Cache;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.PlatformNoGeneratedKeysTest;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Arrays;

/**
 *
 * Test preparation:
 *
 * - Enable file based H2 in ebean.properties (line `datasource.h2.databaseUrl=jdbc:h2:file:~/tests`)
 * - disable logging of io.ebean.SUM/TXN/SQL in logback-test.xml
 * - remove existing database in ~/tests (do that before each run)
 * - run this class with `-Xmx1g` jvm argument
 * - an OOM in iteration ~45000 will occur (45k * 10k chars = 45k * 20k bytes = 900MB)
 *
 * remove the line `@Cache(enableQueryCache=true)` - and it will run forever (until disk is full)
 */
public class MainMemoryLeak {

  @Cache(enableQueryCache=true)
  @Entity
  public static class ECachedBean {
    @Id
    private Long id;

    @Lob
    private String description;
  }

  public static void main(String[] args) {

    PlatformNoGeneratedKeysTest.OtherH2Platform platform = new PlatformNoGeneratedKeysTest.OtherH2Platform();

    DatabaseConfig config = new DatabaseConfig();

    config.setDatabasePlatform(platform);
    config.addClass(ECachedBean.class);
    config.loadFromProperties();

    Database server = DatabaseFactory.create(config);

    // create a string with 10k chars
    char[] c = new char[10_000];
    Arrays.fill(c, 'x');

    try (Transaction txn = server.beginTransaction()) {
      for (int i = 0; i < 500000; i++) {

        if (i % 1000 == 0) {
          long mem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
          if (mem > 1024) {
            throw new IllegalStateException("-Xmx1g JVM argument expected");
          }
          System.out.println("Iteration: " + i + " Free mem: " + mem + " MB");
        }

        ECachedBean a = new ECachedBean();
        a.description = new String(c);
        server.save(a);
      }
      System.out.println("Success, no mem limit detected");
      txn.commit();
    }
    server.shutdown();
  }
}
