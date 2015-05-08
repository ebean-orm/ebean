package com.avaje.ebean.text;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.avaje.ebean.Query;

/**
 * This is a Tree like structure of paths and properties that can be used for
 * defining which parts of an object graph to render in JSON or XML, and can
 * also be used to define which parts to select and fetch for an ORM query.
 * <p>
 * It provides a way of parsing a string representation of nested path
 * properties and applying that to both what to fetch (ORM query) and what to
 * render (JAX-RS JSON / XML).
 * </p>
 */
public class PathProperties {

  private final Map<String, Props> pathMap;

  private final Props rootProps;

  /**
   * Parse and return a PathProperties from nested string format like
   * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
   * path containing "g" and the root path contains "a","b","c" and "f".
   */
  public static PathProperties parse(String source) {
    return PathPropertiesParser.parse(source);
  }

  /**
   * Construct an empty PathProperties.
   */
  public PathProperties() {
    this.rootProps = new Props(this, null, null);
    this.pathMap = new LinkedHashMap<String, Props>();
    this.pathMap.put(null, rootProps);
  }

  /**
   * Construct for creating copy.
   */
  private PathProperties(PathProperties orig) {
    this.rootProps = orig.rootProps.copy(this);
    this.pathMap = new LinkedHashMap<String, Props>(orig.pathMap.size());
    Set<Entry<String, Props>> entrySet = orig.pathMap.entrySet();
    for (Entry<String, Props> e : entrySet) {
      pathMap.put(e.getKey(), e.getValue().copy(this));
    }
  }

  /**
   * Create a copy of this instance so that it can be modified.
   * <p>
   * For example, you may want to create a copy to add extra properties to a
   * path so that they are fetching in a ORM query but perhaps not rendered by
   * default. That is, use a PathProperties for JSON or XML rendering, but
   * create a copy, add some extra properties and then use that copy to define
   * an ORM query.
   * </p>
   */
  public PathProperties copy() {
    return new PathProperties(this);
  }

  /**
   * Return true if there are no paths defined.
   */
  public boolean isEmpty() {
    return pathMap.isEmpty();
  }

  public String toString() {
    return pathMap.toString();
  }

  /**
   * Return true if the path is defined and has properties.
   */
  public boolean hasPath(String path) {
    Props props = pathMap.get(path);
    return props != null && !props.isEmpty();
  }

  /**
   * Get the properties for a given path.
   */
  public Set<String> get(String path) {
    Props props = pathMap.get(path);
    return props == null ? null : props.getProperties();
  }

  public void addToPath(String path, String property) {
    Props props = pathMap.get(path);
    if (props == null) {
      props = new Props(this, null, path);
      pathMap.put(path, props);
    }
    props.getProperties().add(property);
  }

  /**
   * Set the properties for a given path.
   */
  public void put(String path, Set<String> properties) {
    pathMap.put(path, new Props(this, null, path, properties));
  }

  /**
   * Remove a path returning the properties set for that path.
   */
  public Set<String> remove(String path) {
    Props props = pathMap.remove(path);
    return props == null ? null : props.getProperties();
  }

  /**
   * Return a shallow copy of the paths.
   */
  public Set<String> getPaths() {
    return new LinkedHashSet<String>(pathMap.keySet());
  }

  public Collection<Props> getPathProps() {
    return pathMap.values();
  }

  /**
   * Apply these path properties as fetch paths to the query.
   */
  public void apply(Query<?> query) {

    for (Entry<String, Props> entry : pathMap.entrySet()) {
      String path = entry.getKey();
      String props = entry.getValue().getPropertiesAsString();

      if (path == null || path.length() == 0) {
        query.select(props);
      } else {
        query.fetch(path, props);
      }
    }
  }

  protected Props getRootProperties() {
    return rootProps;
  }

  public static class Props {

    private final PathProperties owner;

    private final String parentPath;
    private final String path;

    private final Set<String> propSet;

    private Props(PathProperties owner, String parentPath, String path, Set<String> propSet) {
      this.owner = owner;
      this.path = path;
      this.parentPath = parentPath;
      this.propSet = propSet;
    }

    private Props(PathProperties owner, String parentPath, String path) {
      this(owner, parentPath, path, new LinkedHashSet<String>());
    }

    /**
     * Create a shallow copy of this Props instance.
     */
    public Props copy(PathProperties newOwner) {
      return new Props(newOwner, parentPath, path, new LinkedHashSet<String>(propSet));
    }

    public String getPath() {
      return path;
    }

    public String toString() {
      return propSet.toString();
    }

    public boolean isEmpty() {
      return propSet.isEmpty();
    }

    /**
     * Return the properties for this property set.
     */
    public Set<String> getProperties() {
      return propSet;
    }

    /**
     * Return the properties as a comma delimited string.
     */
    public String getPropertiesAsString() {

      StringBuilder sb = new StringBuilder();

      Iterator<String> it = propSet.iterator();
      boolean hasNext = it.hasNext();
      while (hasNext) {
        sb.append(it.next());
        hasNext = it.hasNext();
        if (hasNext) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    /**
     * Return the parent path
     */
    protected Props getParent() {
      return owner.pathMap.get(parentPath);
    }

    /**
     * Add a child Property set.
     */
    protected Props addChild(String subpath) {

      subpath = subpath.trim();
      addProperty(subpath);

      // build the subpath
      String p = path == null ? subpath : path + "." + subpath;
      Props nested = new Props(owner, path, p);
      owner.pathMap.put(p, nested);
      return nested;
    }

    /**
     * Add a properties to include for this path.
     */
    protected void addProperty(String property) {
      propSet.add(property.trim());
    }
  }

}
