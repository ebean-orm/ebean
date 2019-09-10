package io.ebean.text;

import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.util.SplitName;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
public class PathProperties implements FetchPath {

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
    this.pathMap = new LinkedHashMap<>();
    this.pathMap.put(null, rootProps);
  }

  @Override
  public String toString() {
    return pathMap.toString();
  }

  /**
   * Return true if the path is defined and has properties.
   */
  @Override
  public boolean hasPath(String path) {
    Props props = pathMap.get(path);
    return props != null && !props.isEmpty();
  }

  /**
   * Get the properties for a given path.
   */
  @Override
  public Set<String> getProperties(String path) {
    Props props = pathMap.get(path);
    return props == null ? null : props.getProperties();
  }

  public void addToPath(String path, String property) {
    getProps(path).getProperties().add(property);
  }

  public void addNested(String prefix, PathProperties pathProps) {

    for (Entry<String, Props> entry : pathProps.pathMap.entrySet()) {

      String path = pathAdd(prefix, entry.getKey());
      String[] split = SplitName.split(path);
      getProps(split[0]).addProperty(split[1]);
      getProps(path).addProps(entry.getValue());
    }
  }

  private String pathAdd(String prefix, String key) {
    return key == null ? prefix : prefix + "." + key;
  }

  Props getProps(String path) {
    return pathMap.computeIfAbsent(path, p -> new Props(this, null, p));
  }

  public Collection<Props> getPathProps() {
    return pathMap.values();
  }

  /**
   * Apply these path properties as fetch paths to the query.
   */
  @Override
  public <T> void apply(Query<T> query) {

    for (Entry<String, Props> entry : pathMap.entrySet()) {
      String path = entry.getKey();
      String props = entry.getValue().getPropertiesAsString();

      if (path == null || path.isEmpty()) {
        query.select(props);
      } else {
        query.fetch(path, props);
      }
    }
  }

  protected Props getRootProperties() {
    return rootProps;
  }

  /**
   * Return true if the property (dot notation) is included in the PathProperties.
   */
  public boolean includesProperty(String name) {

    String[] split = SplitName.split(name);
    Props props = pathMap.get(split[0]);
    return (props != null && props.includes(split[1]));
  }

  /**
   * Return true if the property is included using a prefix.
   */
  public boolean includesProperty(String prefix, String name) {
    return includesProperty(SplitName.add(prefix, name));
  }

  /**
   * Return true if the fetch path is included in the PathProperties.
   * <p>
   * The fetch path is a OneToMany or ManyToMany path in dot notation.
   * </p>
   */
  public boolean includesPath(String path) {
    return pathMap.containsKey(path);
  }

  /**
   * Return true if the path is included using a prefix.
   */
  public boolean includesPath(String prefix, String name) {
    return includesPath(SplitName.add(prefix, name));
  }

  public static class Props {

    private final PathProperties owner;

    private final String parentPath;
    private final String path;

    private final LinkedHashSet<String> propSet;

    private Props(PathProperties owner, String parentPath, String path, LinkedHashSet<String> propSet) {
      this.owner = owner;
      this.path = path;
      this.parentPath = parentPath;
      this.propSet = propSet;
    }

    private Props(PathProperties owner, String parentPath, String path) {
      this(owner, parentPath, path, new LinkedHashSet<>());
    }

    public String getPath() {
      return path;
    }

    @Override
    public String toString() {
      return propSet.toString();
    }

    public boolean isEmpty() {
      return propSet.isEmpty();
    }

    /**
     * Return the properties for this property set.
     */
    public LinkedHashSet<String> getProperties() {
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
    protected Props addChild(String subPath) {

      subPath = subPath.trim();
      addProperty(subPath);

      // build the subPath
      String fullPath = path == null ? subPath : path + "." + subPath;
      Props nested = new Props(owner, path, fullPath);
      owner.pathMap.put(fullPath, nested);
      return nested;
    }

    /**
     * Add a properties to include for this path.
     */
    protected void addProperty(String property) {
      propSet.add(property.trim());
    }

    private void addProps(Props value) {
      propSet.addAll(value.propSet);
    }

    private boolean includes(String prop) {
      return propSet.isEmpty() || propSet.contains(prop) || propSet.contains("*");
    }
  }

}
