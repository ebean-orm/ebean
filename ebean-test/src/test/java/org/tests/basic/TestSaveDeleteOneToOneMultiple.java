package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

public class TestSaveDeleteOneToOneMultiple extends BaseTestCase {

//	public void testCreateDeletePFile() {
//		PFile persistentFile = new PFile("test.txt",
//				new PFileContent("test".getBytes(StandardCharsets.UTF_8)));
//
//		DB.save(persistentFile);
//		DB.delete(persistentFile);
//	}

  @Test
  public void testCreateLoadDeletePFile() {
    PFile persistentFile = new PFile("test.txt",
      new PFileContent("test".getBytes(StandardCharsets.UTF_8)));

    DB.save(persistentFile);

    persistentFile = DB.find(PFile.class, persistentFile.getId());

    PFileContent persistentFileContent = persistentFile.getFileContent();

    assertNotNull(persistentFileContent);
    assertNotNull(persistentFileContent.getContent());
    DB.delete(persistentFile);
  }
}
