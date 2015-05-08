package com.avaje.ebeaninternal.server.expression;

import java.io.Serializable;

/**
 * This is the path prefix for filterMany.
 * <p>
 * The actual path can change due to FetchConfig query joins that proceed the
 * query that includes the filterMany.
 * </p>
 */
public class FilterExprPath implements Serializable {

  private static final long serialVersionUID = -6420905565372842018L;

  /**
   * The path of the filterMany.
   */
  private String path;

  public FilterExprPath(String path) {
    this.path = path;
  }

  /**
   * Return a copy of the FilterExprPath trimming off leading part of the path
   * due to a proceeding (earlier) query join etc.
   */
  public FilterExprPath trimPath(int prefixTrim) {
    if (prefixTrim >= path.length()) {
      return new FilterExprPath(null);  
    }
    return new FilterExprPath(path.substring(prefixTrim));
  }

  /**
   * Return the path. This is a prefix used in the filterMany expressions.
   */
  public String getPath() {
    return path;
  }

}
