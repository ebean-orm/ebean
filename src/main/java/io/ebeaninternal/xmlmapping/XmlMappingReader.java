package io.ebeaninternal.xmlmapping;

import io.ebeaninternal.xmlmapping.model.XmEbean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

public class XmlMappingReader {


  /**
   * Read and return a Migration from an xml document.
   */
  public static XmEbean read(InputStream is) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(XmEbean.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return (XmEbean) unmarshaller.unmarshal(is);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
