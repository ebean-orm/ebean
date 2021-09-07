package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.PersistentFile;
import org.tests.model.basic.PersistentFileContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSaveDeleteOneToOne extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {
    PersistentFile persistentFile = new PersistentFile("test.txt",
      new PersistentFileContent("test".getBytes()));

    Ebean.save(persistentFile);
    Ebean.delete(persistentFile);
  }

  @Test
  public void testCreateLoadDeletePersistentFile() {
    PersistentFile persistentFile = new PersistentFile("test.txt",
      new PersistentFileContent("test".getBytes()));

    Ebean.save(persistentFile);

    persistentFile = Ebean.find(PersistentFile.class, persistentFile.getId());

    PersistentFileContent persistentFileContent = persistentFile.getPersistentFileContent();

    assertNotNull(persistentFileContent);
    assertNotNull(persistentFileContent.getContent());

    Ebean.delete(persistentFile);
  }
}
