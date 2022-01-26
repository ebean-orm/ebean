package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.PersistentFile;
import org.tests.model.basic.PersistentFileContent;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

public class TestSaveDeleteOneToOne extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {
    PersistentFile persistentFile = new PersistentFile("test.txt",
      new PersistentFileContent("test".getBytes(StandardCharsets.UTF_8)));

    DB.save(persistentFile);
    DB.delete(persistentFile);
  }

  @Test
  public void testCreateLoadDeletePersistentFile() {
    PersistentFile persistentFile = new PersistentFile("test.txt",
      new PersistentFileContent("test".getBytes(StandardCharsets.UTF_8)));

    DB.save(persistentFile);

    persistentFile = DB.find(PersistentFile.class, persistentFile.getId());

    PersistentFileContent persistentFileContent = persistentFile.getPersistentFileContent();

    assertNotNull(persistentFileContent);
    assertNotNull(persistentFileContent.getContent());

    DB.delete(persistentFile);
  }
}
