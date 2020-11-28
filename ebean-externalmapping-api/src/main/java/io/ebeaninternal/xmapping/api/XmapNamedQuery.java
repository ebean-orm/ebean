package io.ebeaninternal.xmapping.api;

/**
 * External named query.
 */
public class XmapNamedQuery {

  protected final String name;
  protected final String query;

  public XmapNamedQuery(String name, String query) {
    this.name = name;
    this.query = query;
  }

  /**
   * Return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Return the query.
   */
  public String getQuery() {
    return query;
  }
}
