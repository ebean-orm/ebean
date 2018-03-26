package io.ebeaninternal.xmlmapping;

import io.ebeaninternal.xmlmapping.model.XmEbean;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.avaje.classpath.scanner.Resource;

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

  public static List<XmEbean> readByResourceName(ClassLoader classLoader, String resourceName){
    try {
      Enumeration<URL> resources = classLoader.getResources(resourceName);
      List<XmEbean> mappings = new ArrayList<>();
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream is = url.openStream()) {
          mappings.add(XmlMappingReader.read(is));
        }
      }
      return mappings;
    } catch (IOException e) {
      throw new RuntimeException("Error reading ebean xml mapping", e);
    }
  }

  public static List<XmEbean> readByResourceList(ClassLoader classLoader, List<Resource> resourceList){
    try {
      List<XmEbean> mappings = new ArrayList<>();
      for (Resource xmlMappingRes : resourceList) {
        try (InputStream is = new FileInputStream(xmlMappingRes.getLocationOnDisk())) {
          mappings.add(XmlMappingReader.read(is));
        }
      }
      return mappings;
    } catch (IOException e) {
      throw new RuntimeException("Error reading ebean xml mapping", e);
    }
  }
}
