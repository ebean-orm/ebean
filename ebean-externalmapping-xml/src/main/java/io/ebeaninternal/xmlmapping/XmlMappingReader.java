package io.ebeaninternal.xmlmapping;

import io.avaje.classpath.scanner.Resource;
import io.ebeaninternal.xmlmapping.model.XmEbean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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

  /**
   * Read the deployment XML for the given resource name.
   */
  public static List<XmEbean> readByResourceName(ClassLoader classLoader, String resourceName) {
    try {
      Enumeration<URL> resources = classLoader.getResources(resourceName);
      List<XmEbean> mappings = new ArrayList<>();
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream is = openNoCache(url)) {
          mappings.add(XmlMappingReader.read(is));
        }
      }
      return mappings;
    } catch (IOException e) {
      throw new RuntimeException("Error reading ebean xml mapping", e);
    }
  }

  /**
   * Read the deployment XML for the given resources.
   */
  public static List<XmEbean> readByResourceList(List<Resource> resourceList) {
    try {
      List<XmEbean> mappings = new ArrayList<>();
      for (Resource xmlMappingRes : resourceList) {
        try (InputStream is = xmlMappingRes.inputStream()) {
          mappings.add(XmlMappingReader.read(is));
        }
      }
      return mappings;
    } catch (IOException e) {
      throw new RuntimeException("Error reading ebean xml mapping", e);
    }
  }

  private static InputStream openNoCache(URL url) throws IOException {
    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(false);
    return urlConnection.getInputStream();
  }
}
