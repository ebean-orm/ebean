package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDeleteOneToOneMultiple extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));
//    PFile persistentFile = new PFile();
//    persistentFile.setName("test.txt");
//    PFileContent content = new PFileContent();
//    content.setContent("test".getBytes());
//    persistentFile.setFileContent(content);

    Ebean.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getFileContent().getId();

    // should delete file and fileContent
    Ebean.delete(PFile.class, id);

    PFile file1 = Ebean.find(PFile.class, id);
    PFileContent content1 = Ebean.find(PFileContent.class, contentId);

    assertNull(file1);
    assertNull(content1);
  }

}
