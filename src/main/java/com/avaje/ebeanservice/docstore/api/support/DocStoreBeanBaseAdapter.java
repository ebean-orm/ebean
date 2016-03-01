package com.avaje.ebeanservice.docstore.api.support;

import com.avaje.ebean.FetchPath;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.DocStore;
import com.avaje.ebean.annotation.DocStoreEvent;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeanservice.docstore.api.DocStoreBeanAdapter;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateContext;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;
import com.avaje.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import com.avaje.ebeanservice.docstore.api.mapping.DocumentMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base implementation for much of DocStoreBeanAdapter.
 */
public abstract class DocStoreBeanBaseAdapter<T> implements DocStoreBeanAdapter<T> {

  protected final SpiEbeanServer server;

  /**
   * The associated BeanDescriptor.
   */
  protected final BeanDescriptor<T> desc;

  /**
   * The type of index.
   */
  protected final boolean mapped;

  /**
   * Nested path properties defining the doc structure for indexing.
   */
  protected final DocStructure docStructure;

  /**
   * Identifier used in the queue system to identify the index.
   */
  protected final String queueId;

  /**
   * ElasticSearch index type.
   */
  protected final String indexType;

  /**
   * ElasticSearch index name.
   */
  protected final String indexName;

  /**
   * Doc store deployment annotation.
   */
  private final DocStore docStore;

  /**
   * Behavior on insert.
   */
  protected final DocStoreEvent insert;

  /**
   * Behavior on update.
   */
  protected final DocStoreEvent update;

  /**
   * Behavior on delete.
   */
  protected final DocStoreEvent delete;

  /**
   * List of embedded paths from other documents that include this document type.
   * As such an update to this doc type means that those embedded documents need to be updated.
   */
  protected final List<DocStoreEmbeddedInvalidation> embeddedInvalidation = new ArrayList<DocStoreEmbeddedInvalidation>();

  /**
   * Map of properties to 'raw' properties.
   */
  private Map<String, String> sortableMap;


  public DocStoreBeanBaseAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy) {

    this.desc = desc;
    this.server = desc.getEbeanServer();
    this.mapped = deploy.isDocStoreMapped();
    this.docStructure = (!mapped) ? null : derivePathProperties(deploy);
    this.docStore = deploy.getDocStore();
    this.queueId = derive(desc, deploy.getDocStoreQueueId());
    this.indexName = derive(desc, deploy.getDocStoreIndexName());
    this.indexType = derive(desc, deploy.getDocStoreIndexType());
    this.insert = deploy.getDocStoreInsertEvent();
    this.update = deploy.getDocStoreUpdateEvent();
    this.delete = deploy.getDocStoreDeleteEvent();
  }

  @Override
  public DocumentMapping createDocMapping() {

    if (!mapped) return null;

    PathProperties paths = docStructure.doc();

    DocMappingBuilder mappingBuilder = new DocMappingBuilder(paths, docStore);
    desc.docStoreMapping(mappingBuilder, null);

    mappingBuilder.applyMapping();
    prepareMapping(mappingBuilder);

    sortableMap = mappingBuilder.collectSortable();

    docStructure.prepareMany(desc);

    return mappingBuilder.create(queueId, indexName, indexType);
  }

  protected void prepareMapping(DocMappingBuilder mappingBuilder) {
    // do nothing by default
  }

  @Override
  public String getIndexType() {
    return indexType;
  }

  @Override
  public String getIndexName() {
    return indexName;
  }

  @Override
  public void applyPath(Query<T> query) {
    query.apply(docStructure.doc());
  }

  @Override
  public String rawProperty(String property) {

    String rawProperty = sortableMap.get(property);
    return rawProperty == null ? property : rawProperty;
  }

  /**
   * Register invalidation paths for embedded documents.
   */
  @Override
  public void registerPaths() {
    if (mapped) {
      Collection<PathProperties.Props> pathProps = docStructure.doc().getPathProps();
      for (PathProperties.Props pathProp : pathProps) {
        String path = pathProp.getPath();
        if (path != null) {
          BeanDescriptor<?> targetDesc = desc.getBeanDescriptor(path);
          String idName = targetDesc.getIdProperty().getName();
          String fullPath = path + "." + idName;
          targetDesc.docStoreAdapter().registerInvalidationPath(desc.getDocStoreQueueId(), fullPath, pathProp.getProperties());
        }
      }
    }
  }

  /**
   * Register a doc store invalidation listener for the given bean type, path and properties.
   */
  @Override
  public void registerInvalidationPath(String queueId, String path, Set<String> properties) {

    embeddedInvalidation.add(getEmbeddedInvalidation(queueId, path, properties));
  }

  /**
   * Return the DsInvalidationListener based on the properties, path.
   */
  protected DocStoreEmbeddedInvalidation getEmbeddedInvalidation(String queueId, String path, Set<String> properties) {

    if (properties.contains("*")) {
      return new DocStoreEmbeddedInvalidation(queueId, path);
    } else {
      return new DocStoreEmbeddedInvalidationProperties(queueId, path, getPropertyPositions(properties));
    }
  }

  /**
   * Return the property names as property index positions.
   */
  protected int[] getPropertyPositions(Set<String> properties) {
    List<Integer> posList = new ArrayList<Integer>();
    for (String property : properties) {
      BeanProperty prop = desc.getBeanProperty(property);
      if (prop != null) {
        posList.add(prop.getPropertyIndex());
      }
    }
    int[] pos = new int[posList.size()];
    for (int i = 0; i <pos.length; i++) {
      pos[i] = posList.get(i);
    }
    return pos;
  }

  @Override
  public void updateEmbedded(PersistRequestBean<T> request, DocStoreUpdates docStoreUpdates) {
    for (int i = 0; i < embeddedInvalidation.size(); i++) {
      embeddedInvalidation.get(i).embeddedInvalidate(request, docStoreUpdates);
    }
  }

  /**
   * Return the pathProperties which defines the JSON document to index.
   * This can add derived/embedded/nested parts to the document.
   */
  protected DocStructure derivePathProperties(DeployBeanDescriptor<T> deploy) {

    if (!mapped) {
      return null;
    }

    PathProperties pathProps = deploy.getDocStorePathProperties();
    boolean includeByDefault = (pathProps == null);
    if (pathProps  == null) {
      pathProps = new PathProperties();
    }

    return getDocStructure(pathProps, includeByDefault);
  }

  protected DocStructure getDocStructure(PathProperties pathProps, boolean includeByDefault) {

    DocStructure docStructure = new DocStructure(pathProps);
    BeanProperty[] properties = desc.propertiesNonTransient();
    for (int i = 0; i < properties.length; i++) {
      properties[i].docStoreInclude(includeByDefault, docStructure);
    }
    return docStructure;
  }

  public FetchPath getEmbedded(String path) {
    return docStructure.getEmbedded(path);
  }

  public FetchPath getEmbeddedManyRoot(String path) {
    return docStructure.getEmbeddedManyRoot(path);
  }

  @Override
  public boolean isMapped() {
    return mapped;
  }

  @Override
  public String getQueueId() {
    return queueId;
  }

  @Override
  public DocStoreEvent getEvent(PersistRequest.Type persistType, DocStoreEvent txnMode) {

    if (txnMode == null) {
      return getDocStoreEvent(persistType);
    } else if (txnMode == DocStoreEvent.IGNORE) {
      return DocStoreEvent.IGNORE;
    }
    return mapped ? txnMode : getDocStoreEvent(persistType);
  }

  private DocStoreEvent getDocStoreEvent(PersistRequest.Type persistType) {
    switch (persistType) {
      case INSERT:
        return insert;
      case UPDATE:
        return update;
      case DELETE:
        return delete;
      default:
        return DocStoreEvent.IGNORE;
    }
  }

  /**
   * Return the supplied value or default to the bean name lower case.
   */
  protected String derive(BeanDescriptor<T> desc, String suppliedValue) {
    return (suppliedValue != null && suppliedValue.length() > 0) ? suppliedValue : desc.getName().toLowerCase();
  }

  @Override
  public abstract void deleteById(Object idValue, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void index(Object idValue, T entityBean, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void insert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void update(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocStoreUpdateContext txn) throws IOException;

}
