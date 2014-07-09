package com.avaje.ebeaninternal.server.querydefn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SplitName;

/**
 * Represents the internal structure of an Object Relational query.
 * <p>
 * Holds the select() and join() details of a ORM query.
 * </p>
 * <p>
 * It is worth noting that for autoFetch a "tuned fetch info" builds an instance of OrmQueryDetail.
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
  private LinkedHashMap<String, OrmQueryProperties> fetchPaths = new LinkedHashMap<String, OrmQueryProperties>(8);

  private LinkedHashSet<String> includes = new LinkedHashSet<String>(8);

  /**
   * Return a deep copy of the OrmQueryDetail.
   */
  public OrmQueryDetail copy() {
    OrmQueryDetail copy = new OrmQueryDetail();
    copy.baseProps = baseProps.copy();
    for (Map.Entry<String, OrmQueryProperties> entry : fetchPaths.entrySet()) {
      copy.fetchPaths.put(entry.getKey(), entry.getValue().copy());
    }
    copy.includes = new LinkedHashSet<String>(includes);
    return copy;
  }

  /**
   * Calculate the hash for the query plan.
   */
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    if (baseProps == null) {
      builder.add(false);
    } else {
      builder.add(true);
      baseProps.queryPlanHash(request, builder);
    }

    if (fetchPaths != null) {
      for (OrmQueryProperties p : fetchPaths.values()) {
        p.queryPlanHash(request, builder);
      }
    }
  }

  /**
   * Return true if equal in terms of autofetch (select and joins).
   */
  public boolean isAutoFetchEqual(OrmQueryDetail otherDetail) {
    return autofetchPlanHash() == otherDetail.autofetchPlanHash();
  }

  /**
   * Calculate the hash for the query plan.
   */
  private int autofetchPlanHash() {

    int hc = (baseProps == null ? 1 : baseProps.autofetchPlanHash());

    if (fetchPaths != null) {
      for (OrmQueryProperties p : fetchPaths.values()) {
        hc = hc * 31 + p.autofetchPlanHash();
      }
    }

    return hc;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (baseProps != null) {
      sb.append("select ").append(baseProps);
    }
    if (fetchPaths != null) {
      for (OrmQueryProperties join : fetchPaths.values()) {
        sb.append(" fetch ").append(join);
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
    baseProps = new OrmQueryProperties(null, columns);
  }

  public boolean containsProperty(String property) {
    if (baseProps == null) {
      return true;
    } else {
      return baseProps.isIncluded(property);
    }
  }

  /**
   * Set the base / root query properties.
   */
  public void setBase(OrmQueryProperties baseProps) {
    this.baseProps = baseProps;
  }

  public List<OrmQueryProperties> removeSecondaryQueries() {
    return removeSecondaryQueries(false);
  }

  public List<OrmQueryProperties> removeSecondaryLazyQueries() {
    return removeSecondaryQueries(true);
  }

  private List<OrmQueryProperties> removeSecondaryQueries(boolean lazyQuery) {

    ArrayList<String> matchingPaths = new ArrayList<String>(2);

    for (OrmQueryProperties chunk : fetchPaths.values()) {
      boolean match = lazyQuery ? chunk.isLazyFetch() : chunk.isQueryFetch();
      if (match) {
        matchingPaths.add(chunk.getPath());
      }
    }

    if (matchingPaths.size() == 0) {
      return null;
    }

    // sort into depth order to remove
    Collections.sort(matchingPaths);

    // the list of secondary queries
    ArrayList<OrmQueryProperties> props = new ArrayList<OrmQueryProperties>(2);

    for (int i = 0; i < matchingPaths.size(); i++) {
      String path = matchingPaths.get(i);
      includes.remove(path);
      OrmQueryProperties secQuery = fetchPaths.remove(path);
      if (secQuery == null) {
        // the path has already been removed by another
        // secondary query

      } else {
        props.add(secQuery);

        // remove any child properties for this path
        Iterator<OrmQueryProperties> pass2It = fetchPaths.values().iterator();
        while (pass2It.hasNext()) {
          OrmQueryProperties pass2Prop = pass2It.next();
          if (secQuery.isChild(pass2Prop)) {
            // remove join to secondary query from the main query
            // and add to this secondary query
            pass2It.remove();
            includes.remove(pass2Prop.getPath());
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

  public boolean tuneFetchProperties(OrmQueryDetail tunedDetail) {

    boolean tuned = false;

    OrmQueryProperties tunedRoot = tunedDetail.getChunk(null, false);
    if (tunedRoot != null && tunedRoot.hasProperties()) {
      tuned = true;
      baseProps.setTunedProperties(tunedRoot);

      for (OrmQueryProperties tunedChunk : tunedDetail.fetchPaths.values()) {
        OrmQueryProperties chunk = getChunk(tunedChunk.getPath(), false);
        if (chunk != null) {
          // set the properties to select
          chunk.setTunedProperties(tunedChunk);
        } else {
          // add a missing join
          putFetchPath(tunedChunk.copy());
        }
      }
    }
    return tuned;
  }

  /**
   * Matches a join() method of the query.
   */
  public void putFetchPath(OrmQueryProperties chunk) {
    String path = chunk.getPath();
    fetchPaths.put(path, chunk);
    includes.add(path);
  }

  /**
   * Remove all joins and properties.
   * <p>
   * Typically for the row count query.
   * </p>
   */
  public void clear() {
    includes.clear();
    fetchPaths.clear();
  }

  /**
   * Set the fetch properties and configuration for a given path.
   * 
   * @param path
   *          the property to join
   * @param partialProps
   *          the properties on the join property to include
   */
  public OrmQueryProperties addFetch(String path, String partialProps, FetchConfig fetchConfig) {

    OrmQueryProperties chunk = getChunk(path, true);
    chunk.setProperties(partialProps);
    chunk.setFetchConfig(fetchConfig);
    return chunk;
  }

  public void sortFetchPaths(BeanDescriptor<?> d) {

    LinkedHashMap<String, OrmQueryProperties> sorted = new LinkedHashMap<String, OrmQueryProperties>(fetchPaths.size());

    for (OrmQueryProperties p : fetchPaths.values()) {
      sortFetchPaths(d, p, sorted);
    }

    fetchPaths = sorted;
  }

  private void sortFetchPaths(BeanDescriptor<?> d, OrmQueryProperties p,
      LinkedHashMap<String, OrmQueryProperties> sorted) {

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
            String msg = "Path [" + parentPath + "] not valid from " + d.getFullName();
            throw new PersistenceException(msg);
          }
          // add a missing parent path just fetching the Id property
          BeanPropertyAssoc<?> assocOne = (BeanPropertyAssoc<?>) el.getBeanProperty();
          parentProp = new OrmQueryProperties(parentPath, assocOne.getTargetIdProperty());
        }
        if (parentProp != null) {
          sortFetchPaths(d, parentProp, sorted);
        }
        sorted.put(path, p);
      }
    }
  }

  /**
   * Convert 'fetch joins' to 'many' properties over to 'query joins'.
   */
  public void convertManyFetchJoinsToQueryJoins(BeanDescriptor<?> beanDescriptor, String lazyLoadManyPath,
      boolean allowOne, int queryBatch) {

    ArrayList<OrmQueryProperties> manyChunks = new ArrayList<OrmQueryProperties>(3);

    // the name of the many fetch property if there is one
    String manyFetchProperty = null;

    // flag that is set once the many fetch property is chosen
    boolean fetchJoinFirstMany = allowOne;

    sortFetchPaths(beanDescriptor);

    for (String fetchPath : fetchPaths.keySet()) {
      ElPropertyDeploy elProp = beanDescriptor.getElPropertyDeploy(fetchPath);
      if (elProp.containsManySince(manyFetchProperty)) {

        // this is a join to a *ToMany
        OrmQueryProperties chunk = fetchPaths.get(fetchPath);
        if (chunk.isFetchJoin() && !isLazyLoadManyRoot(lazyLoadManyPath, chunk)
            && !hasParentSecJoin(lazyLoadManyPath, chunk)) {
          // this is a 'fetch join' (included in main query)
          if (fetchJoinFirstMany) {
            // letting the first one remain a 'fetch join'
            fetchJoinFirstMany = false;
            manyFetchProperty = fetchPath;
          } else {
            // convert this one over to a 'query join'
            manyChunks.add(chunk);
          }
        }
      }
    }

    for (int i = 0; i < manyChunks.size(); i++) {
      // convert 'fetch joins' over to 'query joins'
      manyChunks.get(i).setQueryFetch(queryBatch, true);
    }
  }

  /**
   * Return true if this is actually the root level of a +query/+lazy loading query.
   */
  private boolean isLazyLoadManyRoot(String lazyLoadManyPath, OrmQueryProperties chunk) {
    if (lazyLoadManyPath != null && lazyLoadManyPath.equals(chunk.getPath())) {
      return true;
    }
    return false;
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
      } else if (!parent.isFetchJoin()) {
        return true;
      } else {
        return hasParentSecJoin(lazyLoadManyPath, parent);
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
      if (baseProps == null) {
        baseProps = new OrmQueryProperties();
      }
      baseProps.setDefaultProperties(desc.getDefaultSelectClause(), desc.getDefaultSelectClauseSet());
    }

    for (OrmQueryProperties joinProps : fetchPaths.values()) {
      if (!joinProps.hasSelectClause()) {
        BeanDescriptor<?> assocDesc = desc.getBeanDescriptor(joinProps.getPath());
        if (assocDesc.hasDefaultSelectClause()) {
          // use the default select clause
          joinProps.setDefaultProperties(assocDesc.getDefaultSelectClause(), assocDesc.getDefaultSelectClauseSet());
        }
      }
    }
  }

  public boolean hasSelectClause() {
    return (baseProps != null && baseProps.hasSelectClause());
  }

  /**
   * Return true if the query detail has neither select properties specified or any joins defined.
   */
  public boolean isEmpty() {
    return fetchPaths.isEmpty() && (baseProps == null || !baseProps.hasProperties());
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
      putFetchPath(props);
      return props;

    } else {
      return props;
    }
  }

  /**
   * Return true if the property is included.
   */
  public boolean includes(String path) {

    OrmQueryProperties chunk = fetchPaths.get(path);

    // may not have fetch properties if just +cache etc
    return chunk != null && !chunk.isCache();
  }

  /**
   * Return the property includes for this detail.
   */
  public HashSet<String> getIncludes() {
    return includes;
  }
}
