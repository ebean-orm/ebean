package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.Assert;
import org.junit.Test;

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

    Assert.assertNull(file1);
    Assert.assertNull(content1);

  }

}
