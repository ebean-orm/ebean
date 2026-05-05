package io.ebeaninternal.server.querydefn;

import io.ebean.FetchConfig;
import io.ebean.event.BeanQueryRequest;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQueryManyJoin;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.el.ElPropertyValue;

import jakarta.persistence.PersistenceException;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the internal structure of an Object Relational query.
 * <p>
 * Holds the select() and join() details of a ORM query.
 * <p>
 * It is worth noting that for AutoTune a "tuned fetch info" builds an instance of OrmQueryDetail.
 * Tuning a query is a matter of replacing an instance of this class with one that has been tuned
 * with select() and join() set.
 */
public final class OrmQueryDetail implements Serializable {

  private static final long serialVersionUID = -2510486880141461807L;

  /**
   * Root level properties.
   */
  private OrmQueryProperties baseProps = new OrmQueryProperties();

  /**
   * Contains the fetch/lazy/query joins and their properties.
   */
  private LinkedHashMap<String, OrmQueryProperties> fetchPaths = new LinkedHashMap<>();

  /**
   * Return a deep copy of the OrmQueryDetail.
   */
  public OrmQueryDetail copy(OrmQueryDetail existing) {
    OrmQueryDetail copy = new OrmQueryDetail();
    copy.baseProps = baseProps.copy();
    for (Map.Entry<String, OrmQueryProperties> entry : fetchPaths.entrySet()) {
      copy.fetchPaths.put(entry.getKey(), entry.getValue().copy());
    }
    if (existing != null) {
      // transfer any existing filterMany expressions
      for (Map.Entry<String, OrmQueryProperties> entry : existing.fetchPaths.entrySet()) {
        var filterMany = entry.getValue().getFilterMany();
        if (filterMany != null) {
          copy.getChunk(entry.getKey(), true).setFilterMany(filterMany);
        }
      }
    }
    return copy;
  }

  /**
   * Add a nested OrmQueryDetail to this detail.
   */
  public void addNested(String path, OrmQueryDetail other, FetchConfig config) {
    fetchProperties(path, other.baseProps, config);
    for (Map.Entry<String, OrmQueryProperties> entry : other.fetchPaths.entrySet()) {
      fetchProperties(path + "." + entry.getKey(), entry.getValue(), entry.getValue().getFetchConfig());
    }
  }

  /**
   * Calculate the hash for the query plan.
   */
  public void queryPlanHash(StringBuilder builder) {
    baseProps.queryPlanHash(builder);
    if (fetchPaths != null) {
      for (OrmQueryProperties p : fetchPaths.values()) {
        p.queryPlanHash(builder);
      }
    }
  }

  /**
   * Return true if equal in terms of autoTune (select and fetch without property ordering).
   */
  public boolean isAutoTuneEqual(OrmQueryDetail otherDetail) {
    if (!isSameByAutoTune(baseProps, otherDetail.baseProps)) {
      return false;
    }
    if (fetchPaths == null) {
      return otherDetail.fetchPaths == null;
    }
    if (fetchPaths.size() != otherDetail.fetchPaths.size()) {
      return false;
    }
    // check without regard to ordering
    for (Map.Entry<String, OrmQueryProperties> entry : fetchPaths.entrySet()) {
      OrmQueryProperties chunk = otherDetail.getChunk(entry.getKey(), false);
      if (!isSameByAutoTune(entry.getValue(), chunk)) {
        return false;
      }
    }

    return true;
  }

  private boolean isSameByAutoTune(OrmQueryProperties p1, OrmQueryProperties p2) {
    return p1 == null ? p2 == null : p1.isSameByAutoTune(p2);
  }

  /**
   * Return the detail in string form.
   */
  public String asString() {
    StringBuilder sb = new StringBuilder();
    if (baseProps.hasProperties()) {
      baseProps.asStringDebug("select ", sb);
    }
    if (fetchPaths != null) {
      for (OrmQueryProperties join : fetchPaths.values()) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        join.asStringDebug("fetch ", sb);
      }
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    throw new RuntimeException("should not use");
  }

  /**
   * set the properties to include on the base / root entity.
   */
  public void select(String properties) {
    baseProps = new OrmQueryProperties(null, properties, null);
  }

  /**
   * Set select properties that are already parsed.
   */
  public void selectProperties(Set<String> properties) {
    baseProps = new OrmQueryProperties(null, properties, OrmQueryProperties.DEFAULT_FETCH);
  }

  void selectProperties(OrmQueryProperties other) {
    baseProps = new OrmQueryProperties(null, other, OrmQueryProperties.DEFAULT_FETCH);
  }

  boolean containsProperty(String property) {
    return baseProps.isIncluded(property);
  }

  /**
   * Set the base query properties to be empty.
   */
  public void setEmptyBase() {
    this.baseProps = new OrmQueryProperties(null, Collections.emptySet());
  }

  /**
   * Set the base / root query properties.
   */
  public void setBase(OrmQueryProperties baseProps) {
    this.baseProps = baseProps;
  }

  List<OrmQueryProperties> removeSecondaryQueries() {
    return removeSecondaryQueries(false);
  }

  List<OrmQueryProperties> removeSecondaryLazyQueries() {
    return removeSecondaryQueries(true);
  }

  private List<OrmQueryProperties> removeSecondaryQueries(boolean lazyQuery) {
    ArrayList<String> matchingPaths = new ArrayList<>(2);
    for (OrmQueryProperties chunk : fetchPaths.values()) {
      boolean match = lazyQuery ? chunk.isLazyFetch() : chunk.isQueryFetch();
      if (match) {
        matchingPaths.add(chunk.getPath());
      }
    }

    if (matchingPaths.isEmpty()) {
      return null;
    }

    // sort into depth order to remove
    Collections.sort(matchingPaths);
    // the list of secondary queries
    ArrayList<OrmQueryProperties> props = new ArrayList<>();

    for (String path : matchingPaths) {
      OrmQueryProperties secQuery = fetchPaths.remove(path);
      if (secQuery != null) {
        props.add(secQuery);

        // remove any child properties for this path
        Iterator<OrmQueryProperties> pass2It = fetchPaths.values().iterator();
        while (pass2It.hasNext()) {
          OrmQueryProperties pass2Prop = pass2It.next();
          if (secQuery.isChild(pass2Prop)) {
            // remove join to secondary query from the main query
            // and add to this secondary query
            pass2It.remove();
            secQuery.add(pass2Prop);
          }
        }
      }
    }

    // Add the secondary queries as select properties
    // to the parent chunk to ensure the foreign keys
    // are included in the query
    for (OrmQueryProperties prop : props) {
      String path = prop.getPath();
      // split into parent and property
      String[] split = SplitName.split(path);
      // add property to parent chunk
      OrmQueryProperties chunk = getChunk(split[0], true);
      chunk.addSecondaryQueryJoin(split[1]);
    }
    return props;
  }

  boolean tuneFetchProperties(OrmQueryDetail tunedDetail) {
    boolean tuned = false;
    OrmQueryProperties tunedRoot = tunedDetail.getChunk(null, false);
    if (tunedRoot != null) {
      tuned = true;
      baseProps = tunedRoot;
      for (OrmQueryProperties tunedChunk : tunedDetail.fetchPaths.values()) {
        fetch(tunedChunk.copy());
      }
    }
    return tuned;
  }

  /**
   * Add or replace the fetch detail.
   */
  void fetch(OrmQueryProperties chunk) {
    String path = chunk.getPath();
    if (path == null) {
      baseProps = chunk;
    } else {
      fetchPaths.put(path, chunk);
    }
  }

  /**
   * Remove all joins and properties. Typically for the row count query.
   */
  public void clear() {
    fetchPaths.clear();
  }

  /**
   * Set the fetch properties and configuration for a given path.
   *
   * @param path         the property to join
   * @param partialProps the properties on the join property to include
   */
  public void fetch(String path, String partialProps, FetchConfig fetchConfig) {
    fetch(new OrmQueryProperties(path, partialProps, fetchConfig));
  }

  /**
   * Set fetch properties that are already parsed.
   */
  public void fetchProperties(String path, Set<String> properties, FetchConfig fetchConfig) {
    fetch(new OrmQueryProperties(path, properties, fetchConfig));
  }

  void fetchProperties(String path, OrmQueryProperties other) {
    fetchProperties(path, other, other.getFetchConfig());
  }

  void fetchProperties(String path, OrmQueryProperties other, FetchConfig fetchConfig) {
    fetch(new OrmQueryProperties(path, other, fetchConfig));
  }

  /**
   * Add for raw sql etc when the properties are already parsed into a set.
   */
  public void fetch(String path, LinkedHashSet<String> properties) {
    fetch(new OrmQueryProperties(path, properties));
  }

  /**
   * Sort the fetch paths into depth order adding any missing parent paths if necessary.
   */
  public void sortFetchPaths(BeanDescriptor<?> d) {
    sortFetchPaths(d, true);
  }

  private void sortFetchPaths(BeanDescriptor<?> d, boolean addIds) {
    if (!fetchPaths.isEmpty()) {
      LinkedHashMap<String, OrmQueryProperties> sorted = new LinkedHashMap<>();
      for (OrmQueryProperties p : fetchPaths.values()) {
        sortFetchPaths(d, p, sorted, addIds);
      }
      fetchPaths = sorted;
    }
  }

  private void sortFetchPaths(BeanDescriptor<?> d, OrmQueryProperties p, LinkedHashMap<String, OrmQueryProperties> sorted, boolean addId) {
    String path = p.getPath();
    if (!sorted.containsKey(path)) {
      String parentPath = p.getParentPath();
      if (parentPath == null || sorted.containsKey(parentPath)) {
        // off root path or parent already ahead in fetch order
        sorted.put(path, p);
      } else {
        OrmQueryProperties parentProp = fetchPaths.get(parentPath);
        if (parentProp == null) {
          ElPropertyValue el = d.elGetValue(parentPath);
          if (el == null) {
            throw new PersistenceException("Path [" + parentPath + "] not valid from " + d.fullName());
          }
          // add a missing parent path just fetching the Id property
          BeanPropertyAssoc<?> assocOne = (BeanPropertyAssoc<?>) el.beanProperty();
          if (addId) {
            parentProp = new OrmQueryProperties(parentPath, assocOne.targetIdProperty());
          } else {
            parentProp = new OrmQueryProperties(parentPath, Collections.emptySet());
          }
        }
        sortFetchPaths(d, parentProp, sorted, addId);
        sorted.put(path, p);
      }
    }
  }

  /**
   * Mark 'fetch joins' to 'many' properties over to 'query joins' where needed.
   *
   * @return The fetch join many property or null
   */
  SpiQueryManyJoin markQueryJoins(BeanDescriptor<?> beanDescriptor, String lazyLoadManyPath, boolean allowOne, boolean addIds) {
    if (fetchPaths.isEmpty()) {
      return null;
    }

    ElPropertyDeploy many = null;
    // the name of the many fetch property if there is one
    String manyFetchProperty = null;
    // flag that is set once the many fetch property is chosen
    boolean fetchJoinFirstMany = allowOne;

    sortFetchPaths(beanDescriptor, addIds);
    List<FetchEntry> pairs = sortByFetchPreference(beanDescriptor);

    for (FetchEntry pair : pairs) {
      ElPropertyDeploy elProp = pair.getElProp();
      if (elProp.containsManySince(manyFetchProperty)) {
        // this is a join to a *ToMany
        OrmQueryProperties chunk = pair.getProperties();
        if (isQueryJoinCandidate(lazyLoadManyPath, chunk)) {
          // this is a 'fetch join' (included in main query)
          if (fetchJoinFirstMany) {
            // letting the first one remain a 'fetch join'
            fetchJoinFirstMany = false;
            manyFetchProperty = pair.getPath();
            chunk.filterManyInline();
            many = elProp;
          } else {
            // convert this one over to a 'query join'
            chunk.markForQueryJoin();
          }
        }
      }
    }
    return many;
  }

  /**
   * Sort the fetch entries taking into account fetchPreference on the path.
   */
  private List<FetchEntry> sortByFetchPreference(BeanDescriptor<?> desc) {
    List<FetchEntry> entries = new ArrayList<>(fetchPaths.size());
    int idx = 0;
    for (Map.Entry<String, OrmQueryProperties> entry : fetchPaths.entrySet()) {
      String fetchPath = entry.getKey();
      ElPropertyDeploy elProp = desc.elPropertyDeploy(fetchPath);
      if (elProp == null) {
        throw new PersistenceException("Invalid fetch path " + fetchPath + " from " + desc.fullName());
      }
      entries.add(new FetchEntry(idx++, fetchPath, elProp, entry.getValue()));
    }
    Collections.sort(entries);
    return entries;
  }

  /**
   * Return true if this path is a candidate for converting to a query join.
   */
  private boolean isQueryJoinCandidate(String lazyLoadManyPath, OrmQueryProperties chunk) {
    return chunk.isFetchJoin()
      && !isLazyLoadManyRoot(lazyLoadManyPath, chunk)
      && !hasParentSecJoin(lazyLoadManyPath, chunk);
  }

  /**
   * Return true if this is actually the root level of a +query/+lazy loading query.
   */
  private boolean isLazyLoadManyRoot(String lazyLoadManyPath, OrmQueryProperties chunk) {
    return lazyLoadManyPath != null && lazyLoadManyPath.equals(chunk.getPath());
  }

  /**
   * If the chunk has a parent that is a query or lazy join. In this case it does not need to be
   * converted.
   */
  private boolean hasParentSecJoin(String lazyLoadManyPath, OrmQueryProperties chunk) {
    OrmQueryProperties parent = getParent(chunk);
    if (parent == null) {
      return false;
    } else {
      if (lazyLoadManyPath != null && lazyLoadManyPath.equals(parent.getPath())) {
        return false;
      } else {
        return !parent.isFetchJoin() || hasParentSecJoin(lazyLoadManyPath, parent);
      }
    }
  }

  /**
   * Return the parent chunk.
   */
  private OrmQueryProperties getParent(OrmQueryProperties chunk) {
    String parentPath = chunk.getParentPath();
    return parentPath == null ? null : fetchPaths.get(parentPath);
  }

  /**
   * Set any default select clauses for the main bean and any joins that have not explicitly defined
   * a select clause.
   * <p>
   * That is this will use FetchType.LAZY to exclude some properties by default.
   * </p>
   */
  public void setDefaultSelectClause(BeanDescriptor<?> desc) {
    if (desc.hasDefaultSelectClause() && !hasSelectClause()) {
      baseProps = new OrmQueryProperties(null, desc.defaultSelectClause());
    }
    for (OrmQueryProperties joinProps : fetchPaths.values()) {
      if (!joinProps.hasSelectClause()) {
        BeanDescriptor<?> assocDesc = desc.descriptor(joinProps.getPath());
        if (assocDesc != null && assocDesc.hasDefaultSelectClause()) {
          fetch(joinProps.getPath(), assocDesc.defaultSelectClause(), joinProps.getFetchConfig());
        }
      }
    }
  }

  public boolean hasSelectClause() {
    return baseProps.hasSelectClause();
  }

  /**
   * Return true if the query detail has neither select properties specified or any joins defined.
   */
  public boolean isEmpty() {
    return fetchPaths.isEmpty() && baseProps.allProperties();
  }

  /**
   * Return true if there are no joins.
   */
  public boolean isJoinsEmpty() {
    return fetchPaths.isEmpty();
  }

  /**
   * Add the explicit bean join.
   * <p>
   * This is also used to Exclude the matching property from the parent select (aka remove the
   * foreign key) because it is now included in it's on node in the SqlTree.
   * </p>
   */
  public void includeBeanJoin(String parentPath, String propertyName) {
    OrmQueryProperties parentChunk = getChunk(parentPath, true);
    parentChunk.includeBeanJoin(propertyName);
  }

  public OrmQueryProperties getChunk(String path, boolean create) {
    if (path == null) {
      return baseProps;
    }
    OrmQueryProperties props = fetchPaths.get(path);
    if (create && props == null) {
      props = new OrmQueryProperties(path);
      fetch(props);
    }
    return props;
  }

  /**
   * Return true if the fetch path is included.
   */
  public boolean includesPath(String path) {
    OrmQueryProperties chunk = fetchPaths.get(path);
    // may not have fetch properties if just +cache etc
    return chunk != null && !chunk.isCache();
  }

  /**
   * Return the fetch paths for this detail.
   */
  public Set<String> getFetchPaths() {
    return fetchPaths.keySet();
  }

  /**
   * Return the underlying fetch path entries.
   */
  public Set<Map.Entry<String, OrmQueryProperties>> entries() {
    return fetchPaths.entrySet();
  }

  /**
   * Prepare filterMany expressions that are being included into the main query.
   */
  public void prepareExpressions(BeanQueryRequest<?> request) {
    for (OrmQueryProperties value : fetchPaths.values()) {
      value.prepareExpressions(request);
    }
  }

  private static class FetchEntry implements Comparable<FetchEntry> {

    private final int index;
    private final String path;
    private final OrmQueryProperties properties;
    private final ElPropertyDeploy elProp;

    FetchEntry(int index, String path, ElPropertyDeploy elProp, OrmQueryProperties value) {
      this.index = index;
      this.path = path;
      this.elProp = elProp;
      this.properties = value;
    }

    String getPath() {
      return path;
    }

    OrmQueryProperties getProperties() {
      return properties;
    }

    ElPropertyDeploy getElProp() {
      return elProp;
    }

    /**
     * Sort by fetchPreference and then by index order.
     */
    @Override
    public int compareTo(FetchEntry other) {
      int fp = elProp.fetchPreference();
      int op = other.elProp.fetchPreference();
      if (fp == op) {
        return Integer.compare(index, other.index);
      } else {
        return (fp < op) ? -1 : 1;
      }
    }
  }
}
