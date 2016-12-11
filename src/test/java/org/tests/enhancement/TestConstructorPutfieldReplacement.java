package org.tests.enhancement;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;
import org.junit.Assert;
import org.junit.Test;

public class TestConstructorPutfieldReplacement extends BaseTestCase {

  @Test
  public void test() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));

    EntityBean eb = (EntityBean) persistentFile;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    int namePos = ebi.findProperty("name");
    int fileContentPos = ebi.findProperty("fileContent");

    Assert.assertTrue(ebi.isLoadedProperty(namePos));
    Assert.assertTrue(ebi.isLoadedProperty(fileContentPos));

  }

}
