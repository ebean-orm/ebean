package io.ebeaninternal.xmlmapping.model;

import io.ebeaninternal.xmlmapping.XmlMappingReader;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class XmlMappingReaderTest {

  @Test
  public void read() throws Exception {

    InputStream is = XmlMappingReaderTest.class.getResourceAsStream("/test-ebean.xml");
    XmEbean testMapping = XmlMappingReader.read(is);

    assertNotNull(testMapping);
  }

}
