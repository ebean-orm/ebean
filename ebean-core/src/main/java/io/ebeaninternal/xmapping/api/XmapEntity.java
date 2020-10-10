package io.ebeaninternal.xmapping.api;

import java.util.ArrayList;
import java.util.List;

public class XmapEntity {

  protected final String clazz;
  protected final List<XmapNamedQuery> namedQuery = new ArrayList<>();
  protected final List<XmapRawSql> rawSql = new ArrayList<>();

  public XmapEntity(String clazz) {
    this.clazz = clazz;
  }

  public String getClazz() {
    return clazz;
  }

  public List<XmapNamedQuery> getNamedQuery() {
    return namedQuery;
  }

  public List<XmapRawSql> getRawSql() {
    return rawSql;
  }
}
