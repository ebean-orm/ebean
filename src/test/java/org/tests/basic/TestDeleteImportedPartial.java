package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.Assert;
import org.junit.Test;

public class TestDeleteImportedPartial extends BaseTestCase {

  @Test
  public void test() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));

    Ebean.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getFileContent().getId();

    PFile partialPfile = Ebean.find(PFile.class).select("id").where().idEq(persistentFile.getId())
      .findOne();

    // should delete file and fileContent
    Ebean.delete(partialPfile);

    PFile file1 = Ebean.find(PFile.class, id);
    PFileContent content1 = Ebean.find(PFileContent.class, contentId);

    Assert.assertNull(file1);
    Assert.assertNull(content1);

  }
}
