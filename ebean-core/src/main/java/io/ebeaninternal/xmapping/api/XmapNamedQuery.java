package io.ebeaninternal.xmapping.api;

public class XmapNamedQuery {

  protected final String name;
  protected final String query;

  public XmapNamedQuery(String name, String query) {
    this.name = name;
    this.query = query;
  }

  public String getName() {
    return name;
  }

  public String getQuery() {
    return query;
  }
}
