package io.ebeaninternal.xmlmapping;

import io.avaje.classpath.scanner.ClassPathScanner;
import io.avaje.classpath.scanner.ClassPathScannerFactory;
import io.avaje.classpath.scanner.Resource;
import io.ebeaninternal.xmapping.api.XmapEbean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Reads the Xml deployment information.
 */
class InternalConfigXmlRead {

  private static final Logger log = LoggerFactory.getLogger(InternalConfigXmlRead.class);

  private final ToXmapEbean to = new ToXmapEbean();
  private final ClassLoader classLoader;
  private final List<String> mappingLocations;

  InternalConfigXmlRead(ClassLoader classLoader, List<String> mappingLocations) {
    this.classLoader = classLoader;
    this.mappingLocations = mappingLocations;
//    if (config.getClassLoadConfig().isJavaxJAXBPresent()) {
//      init();
//    }
  }

  List<XmapEbean> read() {
    List<XmapEbean> list = new ArrayList<>();
    list.addAll(to.toBeans(XmlMappingReader.readByResourceName(classLoader, "ebean.xml")));
    list.addAll(to.toBeans(XmlMappingReader.readByResourceList(xmlMappingResources())));
    return list;
  }

  private List<Resource> xmlMappingResources() {
    List<ClassPathScanner> scanners = scanners();
    List<Resource> resourceList = new ArrayList<>();
    long st = System.currentTimeMillis();
    if (mappingLocations != null && !mappingLocations.isEmpty()) {
      for (ClassPathScanner finder : scanners) {
        for (String mappingLocation : mappingLocations) {
          resourceList.addAll(finder.scanForResources(mappingLocation, resourceName -> resourceName.endsWith(".xml")));
        }
      }
    }
    long searchTime = System.currentTimeMillis() - st;
    log.debug("Classpath search mappings[{}] searchTime[{}]", resourceList.size(), searchTime);
    return resourceList;
  }

  /**
   * Return the list of ClassPathScanner services using DatabaseConfig service loader.
   */
  private List<ClassPathScanner> scanners() {
    List<ClassPathScanner> scanners = new ArrayList<>();
    for (ClassPathScannerFactory factory : ServiceLoader.load(ClassPathScannerFactory.class, classLoader)) {
      scanners.add(factory.createScanner(classLoader));
    }
    return scanners;
  }
}
