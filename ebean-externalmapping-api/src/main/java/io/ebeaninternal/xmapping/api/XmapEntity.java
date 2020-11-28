package io.ebeaninternal.xmapping.api;

import java.util.ArrayList;
import java.util.List;

/**
 * External mapping for an Entity.
 */
public class XmapEntity {

  protected final String clazz;
  protected final List<XmapNamedQuery> namedQuery = new ArrayList<>();
  protected final List<XmapRawSql> rawSql = new ArrayList<>();

  public XmapEntity(String clazz) {
    this.clazz = clazz;
  }

  /**
   * Return the entity class.
   */
  public String getClazz() {
    return clazz;
  }

  /**
   * Return the named queries for this entity.
   */
  public List<XmapNamedQuery> getNamedQuery() {
    return namedQuery;
  }

  /**
   * Return the named raw sql queries for this entity.
   */
  public List<XmapRawSql> getRawSql() {
    return rawSql;
  }
}
