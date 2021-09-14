package io.ebeaninternal.xmlmapping.model;

import io.ebeaninternal.xmlmapping.XmlMappingReader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XmlMappingReaderTest {

  @Test
  public void read() {

    InputStream is = XmlMappingReaderTest.class.getResourceAsStream("/test-ebean.xml");
    XmEbean testMapping = XmlMappingReader.read(is);

    assertNotNull(testMapping);
  }

}
