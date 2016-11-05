package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.FetchConfig;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SplitName;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the internal structure of an Object Relational query.
 * <p>
 * Holds the select() and join() details of a ORM query.
 * </p>
 * <p>
 * It is worth noting that for AutoTune a "tuned fetch info" builds an instance of OrmQueryDetail.
 * Tuning a query is a matter of replacing an instance of this class with one that has been tuned
 * with select() and join() set.
 * </p>
 */
public class OrmQueryDetail implements Serializable {

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
  public OrmQueryDetail copy() {
    OrmQueryDetail copy = new OrmQueryDetail();
    copy.baseProps = baseProps.copy();
    for (Map.Entry<String, OrmQueryProperties> entry : fetchPaths.entrySet()) {
      copy.fetchPaths.put(entry.getKey(), entry.getValue().copy());
    }
    return copy;
  }

  public int queryPlanHash() {
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();
    queryPlanHash(builder);
    return builder.getPlanHash();
  }

  /**
   * Calculate the hash for the query plan.
   */
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    baseProps.queryPlanHash(builder);
    if (fetchPaths != null) {
      for (OrmQueryProperties p : fetchPaths.values()) {
        p.queryPlanHash(builder);
      }
    }
  }

  /**
   * Return true if the details are the same for query plan purposes.
   */
  public boolean isSameByPlan(OrmQueryDetail otherDetail) {
    if (!isSameByPlan(baseProps, otherDetail.baseProps)) {
      return false;
    }
    if (fetchPaths == null) {
      return otherDetail.fetchPaths == null;
    }
    if (fetchPaths.size() != otherDetail.fetchPaths.size()) {
      return false;
    }
    // check with ordering being important
    Iterator<Map.Entry<String, OrmQueryProperties>> thisIt = fetchPaths.entrySet().iterator();
    Iterator<Map.Entry<String, OrmQueryProperties>> thatIt = otherDetail.fetchPaths.entrySet().iterator();
    while (thisIt.hasNext() && thatIt.hasNext()) {
      Map.Entry<String, OrmQueryProperties> thisEntry = thisIt.next();
      Map.Entry<String, OrmQueryProperties> thatEntry = thatIt.next();
      if (!thisEntry.getKey().equals(thatEntry.getKey())) {
        return false;
      }
      if (!thisEntry.getValue().isSameByPlan(thatEntry.getValue())) {
        return false;
      }
    }

    return true;
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

  private boolean isSameByPlan(OrmQueryProperties p1, OrmQueryProperties p2) {
    return p1 == null ? p2 == null : p1.isSameByPlan(p2);
  }

  public String toString() {
    return asString();
  }

  /**
   * Return the detail in string form.
   */
  public String asString() {
    StringBuilder sb = new StringBuilder();
    if (!baseProps.isEmpty()) {
      baseProps.append("select ", sb);
    }
    if (fetchPaths != null) {
      for (OrmQueryProperties join : fetchPaths.values()) {
        join.append(" fetch ", sb);
      }
    }
    return sb.toString();
  }

  public int hashCode() {
    throw new RuntimeException("should not use");
  }

  /**
   * set the properties to include on the base / root entity.
   */
  public void select(String columns) {
    baseProps = new OrmQueryProperties(null, columns, null);
  }

  boolean containsProperty(String property) {
    return baseProps.isIncluded(property);
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

    for (int i = 0; i < matchingPaths.size(); i++) {
      String path = matchingPaths.get(i);
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
    for (int i = 0; i < props.size(); i++) {
      String path = props.get(i).getPath();
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
  protected void fetch(OrmQueryProperties chunk) {
    String path = chunk.getPath();
    if (path == null) {
      baseProps = chunk;
    } else {
      fetchPaths.put(path, chunk);
    }
  }

  /**
   * Remove all joins and properties.
   * <p>
   * Typically for the row count query.
   * </p>
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
   * Add for raw sql etc when the properties are already parsed into a set.
   */
  public void fetch(String path, LinkedHashSet<String> properties) {
    fetch(new OrmQueryProperties(path, properties));
  }

  /**
   * Sort the fetch paths into depth order adding any missing parent paths if necessary.
   */
  public void sortFetchPaths(BeanDescriptor<?> d) {

    if (!fetchPaths.isEmpty()) {
      LinkedHashMap<String, OrmQueryProperties> sorted = new LinkedHashMap<>();
      for (OrmQueryProperties p : fetchPaths.values()) {
        sortFetchPaths(d, p, sorted);
      }
      fetchPaths = sorted;
    }
  }

  private void sortFetchPaths(BeanDescriptor<?> d, OrmQueryProperties p, LinkedHashMap<String, OrmQueryProperties> sorted) {

    String path = p.getPath();
    if (!sorted.containsKey(path)) {
      String parentPath = p.getParentPath();
      if (parentPath == null || sorted.containsKey(parentPath)) {
        // off root path or parent already ahead in fetch order
        sorted.put(path, p);
      } else {
        OrmQueryProperties parentProp = fetchPaths.get(parentPath);
        if (parentProp == null) {
          ElPropertyValue el = d.getElGetValue(parentPath);
          if (el == null) {
            throw new PersistenceException("Path [" + parentPath + "] not valid from " + d.getFullName());
          }
          // add a missing parent path just fetching the Id property
          BeanPropertyAssoc<?> assocOne = (BeanPropertyAssoc<?>) el.getBeanProperty();
          parentProp = new OrmQueryProperties(parentPath, assocOne.getTargetIdProperty());
        }

        sortFetchPaths(d, parentProp, sorted);
        sorted.put(path, p);
      }
    }
  }

  /**
   * Mark 'fetch joins' to 'many' properties over to 'query joins' where needed.
   */
  void markQueryJoins(BeanDescriptor<?> beanDescriptor, String lazyLoadManyPath, boolean allowOne) {

    // the name of the many fetch property if there is one
    String manyFetchProperty = null;

    // flag that is set once the many fetch property is chosen
    boolean fetchJoinFirstMany = allowOne;

    sortFetchPaths(beanDescriptor);

    for (String fetchPath : fetchPaths.keySet()) {
      ElPropertyDeploy elProp = beanDescriptor.getElPropertyDeploy(fetchPath);
      if (elProp == null) {
        throw new PersistenceException("Invalid fetch path " + fetchPath + " from " + beanDescriptor.getFullName());
      }
      if (elProp.containsManySince(manyFetchProperty)) {

        // this is a join to a *ToMany
        OrmQueryProperties chunk = fetchPaths.get(fetchPath);
        if (isQueryJoinCandidate(lazyLoadManyPath, chunk)) {
          // this is a 'fetch join' (included in main query)
          if (fetchJoinFirstMany) {
            // letting the first one remain a 'fetch join'
            fetchJoinFirstMany = false;
            manyFetchProperty = fetchPath;
          } else {
            // convert this one over to a 'query join'
            chunk.markForQueryJoin();
          }
        }
      }
    }
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
      baseProps = new OrmQueryProperties(null, desc.getDefaultSelectClause());
    }

    for (OrmQueryProperties joinProps : fetchPaths.values()) {
      if (!joinProps.hasSelectClause()) {
        BeanDescriptor<?> assocDesc = desc.getBeanDescriptor(joinProps.getPath());
        if (assocDesc.hasDefaultSelectClause()) {
          fetch(joinProps.getPath(), assocDesc.getDefaultSelectClause(), joinProps.getFetchConfig());
        }
      }
    }
  }

  private boolean hasSelectClause() {
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
      return props;

    } else {
      return props;
    }
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
}
