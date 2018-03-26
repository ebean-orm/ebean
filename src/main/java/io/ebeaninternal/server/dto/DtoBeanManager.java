package io.ebeaninternal.server.dto;

import io.ebean.RawSqlBuilder;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.xmlmapping.model.XmAliasMapping;
import io.ebeaninternal.xmlmapping.model.XmColumnMapping;
import io.ebeaninternal.xmlmapping.model.XmDto;
import io.ebeaninternal.xmlmapping.model.XmEbean;
import io.ebeaninternal.xmlmapping.model.XmNamedQuery;
import io.ebeaninternal.xmlmapping.model.XmRawSql;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all the DTO bean descriptors.
 */
public class DtoBeanManager {

  private static final Logger logger = LoggerFactory.getLogger(DtoBeanManager.class);

  private final TypeManager typeManager;

  private final Map<Class, DtoBeanDescriptor> descriptorMap = new ConcurrentHashMap<>();

  public DtoBeanManager(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  /**
   * Return the descriptor for the given DTO bean class.
   */
  @SuppressWarnings("unchecked")
  public <T> DtoBeanDescriptor<T> getDescriptor(Class<T> dtoType) {

    return descriptorMap.computeIfAbsent(dtoType, this::createDescriptor);
  }

  private <T> DtoBeanDescriptor createDescriptor(Class<T> dtoType) {

    try {
      DtoMeta meta = new DtoMetaBuilder(dtoType, typeManager).build();
      return new DtoBeanDescriptor<>(dtoType, meta);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public void visitMetrics(MetricVisitor visitor) {
    for (DtoBeanDescriptor value : descriptorMap.values()) {
      value.visit(visitor);
    }
  }

  public void readXmlMapping(ClassLoader classLoader, List<XmEbean> mappings) {
    for (XmEbean mapping : mappings) {
      List<XmDto> dtoList = mapping.getDto();
      for (XmDto dto : dtoList) {
        readDtoMapping(classLoader, dto);
      }
    }
  }

  private void readDtoMapping(ClassLoader classLoader, XmDto dto) {
    String dtoClassName = dto.getClazz();
    Class<?> dtoClass;
    try {
      dtoClass = Class.forName(dtoClassName, false, classLoader);
    } catch (Exception e) {
      logger.error("Could not load dto bean class " + dtoClassName + " for ebean xml entry");
      return;
    }

    DtoBeanDescriptor<?> dtoBeanDescriptor = getDescriptor(dtoClass);
    if (dtoBeanDescriptor == null) {
      logger.error("No dto bean for ebean xml entry " + dtoClass);

    } else {
      for (XmRawSql sql : dto.getRawSql()) {
        RawSqlBuilder builder = RawSqlBuilder.parse(sql.getQuery().getValue());
        for (XmColumnMapping columnMapping : sql.getColumnMapping()) {
          builder.columnMapping(columnMapping.getColumn(), columnMapping.getProperty());
        }
        for (XmAliasMapping aliasMapping : sql.getAliasMapping()) {
          builder.tableAliasMapping(aliasMapping.getAlias(), aliasMapping.getProperty());
        }
        dtoBeanDescriptor.addRawSql(sql.getName(), (SpiRawSql)builder.create());
      }

      for (XmNamedQuery namedQuery : dto.getNamedQuery()) {
        dtoBeanDescriptor.addNamedQuery(namedQuery.getName(), namedQuery.getQuery().getValue());
      }
    }
  }
}
