package io.ebean.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identifies a unique node of an object graph.
 * <p>
 * It represents a location relative to the root of an object graph and specific
 * to a query and call stack hash.
 */
public final class ObjectGraphNode implements Serializable {

  private static final long serialVersionUID = 2087081778650228996L;

  /**
   * Identifies the origin.
   */
  private final ObjectGraphOrigin origin;

  /**
   * The path relative to the root.
   */
  private final String path;

  /**
   * Create at a sub level.
   */
  public ObjectGraphNode(ObjectGraphNode parent, String path) {
    this.origin = parent.origin();
    this.path = parent.childPath(path);
  }

  /**
   * Create an the root level.
   */
  public ObjectGraphNode(ObjectGraphOrigin origin, String path) {
    this.origin = origin;
    this.path = path;
  }

  /**
   * Return the origin query point.
   */
  public ObjectGraphOrigin origin() {
    return origin;
  }

  private String childPath(String childPath) {
    if (path == null) {
      return childPath;
    } else if (childPath == null) {
      return path;
    } else {
      return path + "." + childPath;
    }
  }

  /**
   * Return the path relative to the root.
   */
  public String path() {
    return path;
  }

  @Override
  public String toString() {
    return "origin:" + origin + " path[" + path + "]";
  }

  @Override
  public int hashCode() {
    int hc = 92821 * origin.hashCode();
    hc = 92821 * hc + (path == null ? 0 : path.hashCode());
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ObjectGraphNode)) {
      return false;
    }

    ObjectGraphNode e = (ObjectGraphNode) obj;
    return (Objects.equals(e.path, path))
      && e.origin.equals(origin);
  }
}
