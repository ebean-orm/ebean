package io.ebeaninternal.xmapping.api;

import java.util.ArrayList;
import java.util.List;

/**
 * External DTO mapping.
 */
public class XmapDto {

  protected final String clazz;
  protected final List<XmapRawSql> rawSql = new ArrayList<>();

  public XmapDto(String clazz) {
    this.clazz = clazz;
  }

  public String getClazz() {
    return clazz;
  }

  public List<XmapRawSql> getRawSql() {
    return rawSql;
  }

}
