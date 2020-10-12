package io.ebeaninternal.xmlmapping;

import io.ebeaninternal.xmapping.api.XmapDto;
import io.ebeaninternal.xmapping.api.XmapEbean;
import io.ebeaninternal.xmapping.api.XmapEntity;
import io.ebeaninternal.xmapping.api.XmapNamedQuery;
import io.ebeaninternal.xmapping.api.XmapRawSql;
import io.ebeaninternal.xmlmapping.model.XmAliasMapping;
import io.ebeaninternal.xmlmapping.model.XmColumnMapping;
import io.ebeaninternal.xmlmapping.model.XmDto;
import io.ebeaninternal.xmlmapping.model.XmEbean;
import io.ebeaninternal.xmlmapping.model.XmEntity;
import io.ebeaninternal.xmlmapping.model.XmNamedQuery;
import io.ebeaninternal.xmlmapping.model.XmRawSql;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ToXmapEbean {

  public List<XmapEbean> toBeans(List<XmEbean> xmBeans) {
    return xmBeans.stream()
      .map(this::toBean)
      .collect(toList());
  }

  private XmapEbean toBean(XmEbean bean) {
    XmapEbean xmap = new XmapEbean();
    xmap.getDto().addAll(toDto(bean.getDto()));
    xmap.getEntity().addAll(toEntity(bean.getEntity()));
    return xmap;
  }

  private List<XmapEntity> toEntity(List<XmEntity> xmEntity) {
    return xmEntity.stream()
      .map(this::toEntity)
      .collect(toList());
  }

  private XmapEntity toEntity(XmEntity xmEntity) {
    XmapEntity entity = new XmapEntity(xmEntity.getClazz());
    for (XmNamedQuery named : xmEntity.getNamedQuery()) {
      entity.getNamedQuery().add(toNamed(named));
    }
    for (XmRawSql xmRawSql : xmEntity.getRawSql()) {
      entity.getRawSql().add(toRaw(xmRawSql));
    }
    return entity;
  }

  private XmapNamedQuery toNamed(XmNamedQuery named) {
    return new XmapNamedQuery(named.getName(), named.getQuery().getValue());
  }

  private List<XmapDto> toDto(List<XmDto> dto) {
    return dto.stream()
      .map(this::toDto)
      .collect(toList());
  }

  private XmapDto toDto(XmDto xmapDto) {
    XmapDto dto = new XmapDto(xmapDto.getClazz());
    dto.getRawSql().addAll(toRaw(xmapDto.getRawSql()));
    return dto;
  }

  private List<XmapRawSql> toRaw(List<XmRawSql> rawSql) {
    return rawSql.stream()
      .map(this::toRaw)
      .collect(toList());
  }

  private XmapRawSql toRaw(XmRawSql xmRawSql) {
    XmapRawSql rawSql = new XmapRawSql(xmRawSql.getName(), xmRawSql.getQuery().getValue());
    for (XmColumnMapping xmCol : xmRawSql.getColumnMapping()) {
      rawSql.addColumnMapping(xmCol.getColumn(), xmCol.getProperty());
    }
    for (XmAliasMapping xmAlias : xmRawSql.getAliasMapping()) {
      rawSql.addAliasMapping(xmAlias.getAlias(), xmAlias.getProperty());
    }
    return rawSql;
  }

}
