package org.tests.enhancement;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.PFile;
import org.tests.model.basic.PFileContent;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConstructorPutfieldReplacement extends BaseTestCase {

  @Test
  public void test() {

    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes(StandardCharsets.UTF_8)));

    EntityBean eb = (EntityBean) persistentFile;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    int namePos = ebi.findProperty("name");
    int fileContentPos = ebi.findProperty("fileContent");

    assertTrue(ebi.isLoadedProperty(namePos));
    assertTrue(ebi.isLoadedProperty(fileContentPos));
  }

}
