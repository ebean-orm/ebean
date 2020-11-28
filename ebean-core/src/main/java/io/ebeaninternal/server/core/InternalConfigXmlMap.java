package io.ebeaninternal.server.core;

import io.ebeaninternal.server.dto.DtoNamedQueries;
import io.ebeaninternal.xmapping.api.XmapDto;
import io.ebeaninternal.xmapping.api.XmapEbean;
import io.ebeaninternal.xmapping.api.XmapRawSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the Xml deployment information.
 */
class InternalConfigXmlMap {

  private static final Logger log = LoggerFactory.getLogger(InternalConfigXmlMap.class);

  private final List<XmapEbean> xmlEbeanList;
  private final ClassLoader classLoader;
  private final Map<Class<?>, DtoNamedQueries> dtoNamedQueries = new HashMap<>();

  InternalConfigXmlMap(List<XmapEbean> xmlEbeanList, ClassLoader classLoader) {
    this.xmlEbeanList = xmlEbeanList;
    this.classLoader = classLoader;
    initDtoMapping();
  }

  void initDtoMapping() {
    if (xmlEbeanList != null) {
      for (XmapEbean mapping : xmlEbeanList) {
        List<XmapDto> dtoList = mapping.getDto();
        for (XmapDto dto : dtoList) {
          readDtoMapping(dto);
        }
      }
    }
  }

  /**
   * Return the XML deployment information for entity beans.
   */
  List<XmapEbean> xmlDeployment() {
    return xmlEbeanList;
  }

  /**
   * Return the named queries for Dto beans.
   */
  Map<Class<?>, DtoNamedQueries> readDtoMapping() {
    return dtoNamedQueries;
  }

  private void readDtoMapping(XmapDto dto) {
    Class<?> dtoClass;
    try {
      dtoClass = Class.forName(dto.getClazz(), false, classLoader);
    } catch (Exception e) {
      log.error("Could not load dto bean class " + dto.getClazz() + " for ebean xml entry");
      return;
    }
    DtoNamedQueries namedQueries = dtoNamedQueries.computeIfAbsent(dtoClass, aClass -> new DtoNamedQueries());
    for (XmapRawSql sql : dto.getRawSql()) {
      namedQueries.addRawSql(sql.getName(), sql.getQuery());
    }
  }

}
