package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.dto.DtoNamedQueries;
import io.ebeaninternal.xmlmapping.XmlMappingReader;
import io.ebeaninternal.xmlmapping.model.XmDto;
import io.ebeaninternal.xmlmapping.model.XmEbean;
import io.ebeaninternal.xmlmapping.model.XmRawSql;
import org.avaje.classpath.scanner.ClassPathScanner;
import org.avaje.classpath.scanner.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the Xml deployment information.
 */
class InternalConfigXmlRead {

  private static final Logger log = LoggerFactory.getLogger(InternalConfigXmlRead.class);

  private final ServerConfig serverConfig;

  private final ClassLoader classLoader;

  private final Map<Class<?>, DtoNamedQueries> dtoNamedQueries = new HashMap<>();

  private List<XmEbean> xmlEbeanList;

  InternalConfigXmlRead(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    this.classLoader = serverConfig.getClassLoadConfig().getClassLoader();
    if (serverConfig.getClassLoadConfig().isJavaxJAXBPresent()) {
      init();
    }
  }

  private void init() {
    this.xmlEbeanList = XmlMappingReader.readByResourceName(classLoader, "ebean.xml");
    xmlEbeanList.addAll(XmlMappingReader.readByResourceList(xmlMappingResources()));
  }

  private List<Resource> xmlMappingResources() {
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

  /**
   * Return the XML deployment information for entity beans.
   */
  List<XmEbean> xmlDeployment() {
    return xmlEbeanList;
  }

  /**
   * Return the named queries for Dto beans.
   */
  Map<Class<?>, DtoNamedQueries> readDtoMapping() {
    if (xmlEbeanList != null) {
      for (XmEbean mapping : xmlEbeanList) {
        List<XmDto> dtoList = mapping.getDto();
        for (XmDto dto : dtoList) {
          readDtoMapping(dto);
        }
      }
    }

    return dtoNamedQueries;
  }

  private void readDtoMapping(XmDto dto) {

    String dtoClassName = dto.getClazz();
    Class<?> dtoClass;
    try {
      dtoClass = Class.forName(dtoClassName, false, classLoader);
    } catch (Exception e) {
      log.error("Could not load dto bean class " + dtoClassName + " for ebean xml entry");
      return;
    }

    DtoNamedQueries namedQueries = dtoNamedQueries.computeIfAbsent(dtoClass, aClass -> new DtoNamedQueries());

    for (XmRawSql sql : dto.getRawSql()) {
      namedQueries.addRawSql(sql.getName(), sql.getQuery().getValue());
    }

    if (!dto.getNamedQuery().isEmpty()) {
      log.error("Only raw-sql named queries supported for DTO beans - bean:" + dtoClass);
    }
  }

}
