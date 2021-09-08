package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDeleteImportedPartial extends BaseTestCase {

  @Test
  public void test() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));

    DB.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getFileContent().getId();

    PFile partialPfile = DB.find(PFile.class).select("id").where().idEq(persistentFile.getId())
      .findOne();

    // should delete file and fileContent
    DB.delete(partialPfile);

    PFile file1 = DB.find(PFile.class, id);
    PFileContent content1 = DB.find(PFileContent.class, contentId);

    assertNull(file1);
    assertNull(content1);
  }
}
