package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

public class TestDeleteOneToOneMultiple extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes(StandardCharsets.UTF_8)));
//    PFile persistentFile = new PFile();
//    persistentFile.setName("test.txt");
//    PFileContent content = new PFileContent();
//    content.setContent("test".getBytes(StandardCharsets.UTF_8));
//    persistentFile.setFileContent(content);

    DB.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getFileContent().getId();

    // should delete file and fileContent
    DB.delete(PFile.class, id);

    PFile file1 = DB.find(PFile.class, id);
    PFileContent content1 = DB.find(PFileContent.class, contentId);

    assertNull(file1);
    assertNull(content1);
  }

}
