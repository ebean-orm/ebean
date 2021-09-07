package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSaveDeleteOneToOneMultiple extends BaseTestCase {

//	public void testCreateDeletePFile() {
//		PFile persistentFile = new PFile("test.txt",
//				new PFileContent("test".getBytes()));
//
//		DB.save(persistentFile);
//		DB.delete(persistentFile);
//	}

  @Test
  public void testCreateLoadDeletePFile() {
    PFile persistentFile = new PFile("test.txt",
      new PFileContent("test".getBytes()));

    DB.save(persistentFile);

    persistentFile = DB.find(PFile.class, persistentFile.getId());

    PFileContent persistentFileContent = persistentFile.getFileContent();

    assertNotNull(persistentFileContent);
    assertNotNull(persistentFileContent.getContent());
    DB.delete(persistentFile);
  }
}
