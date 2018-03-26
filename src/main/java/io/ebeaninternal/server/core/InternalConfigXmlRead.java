package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.xmlmapping.XmlMappingReader;
import io.ebeaninternal.xmlmapping.model.XmEbean;
import org.avaje.classpath.scanner.ClassPathScanner;
import org.avaje.classpath.scanner.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class InternalConfigXmlRead {

  private static final Logger log = LoggerFactory.getLogger(InternalConfigXmlRead.class);

  private final ServerConfig serverConfig;

  InternalConfigXmlRead(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  List<XmEbean> build() {

    ClassLoader classLoader = serverConfig.getClassLoadConfig().getClassLoader();
    List<XmEbean> xmlEbeanList = XmlMappingReader.readByResourceName(classLoader, "ebean.xml");

    List<Resource> resources = searchXmlMapping();
    xmlEbeanList.addAll(XmlMappingReader.readByResourceList(resources));

    return xmlEbeanList;
  }


  private List<Resource> searchXmlMapping() {
    List<ClassPathScanner> scanners = ClassPathScanners.find(serverConfig);
    List<String> mappingLocations = serverConfig.getMappingLocations();
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


}
