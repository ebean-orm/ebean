package io.ebeanservice.docstore.api.support;

import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.annotation.DocStore;
import io.ebean.annotation.DocStoreMode;
import io.ebean.docstore.DocUpdateContext;
import io.ebean.plugin.BeanType;
import io.ebean.text.PathProperties;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import io.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import io.ebeanservice.docstore.api.mapping.DocumentMapping;

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
  protected final DocStoreMode insert;

  /**
   * Behavior on update.
   */
  protected DocStoreMode update;

  /**
   * Behavior on delete.
   */
  protected final DocStoreMode delete;

  /**
   * List of embedded paths from other documents that include this document type.
   * As such an update to this doc type means that those embedded documents need to be updated.
   */
  protected final List<DocStoreEmbeddedInvalidation> embeddedInvalidation = new ArrayList<>();

  protected final PathProperties pathProps;

  /**
   * Map of properties to 'raw' properties.
   */
  protected Map<String, String> sortableMap;

  /**
   * Nested path properties defining the doc structure for indexing.
   */
  protected DocStructure docStructure;

  protected DocumentMapping documentMapping;

  private boolean registerPaths;

  public DocStoreBeanBaseAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy) {
    this.desc = desc;
    this.server = desc.ebeanServer();
    this.mapped = deploy.isDocStoreMapped();
    this.pathProps = deploy.getDocStorePathProperties();
    this.docStore = deploy.getDocStore();
    this.queueId = derive(desc, deploy.getDocStoreQueueId());
    this.indexName = derive(desc, deploy.getDocStoreIndexName());
    this.indexType = derive(desc, deploy.getDocStoreIndexType());
    this.insert = deploy.getDocStoreInsertEvent();
    this.update = deploy.getDocStoreUpdateEvent();
    this.delete = deploy.getDocStoreDeleteEvent();
  }

  @Override
  public boolean hasEmbeddedInvalidation() {
    return !embeddedInvalidation.isEmpty();
  }

  @Override
  public DocumentMapping createDocMapping() {
    if (documentMapping != null) {
      return documentMapping;
    }

    if (!mapped) return null;

    this.docStructure = derivePathProperties(pathProps);

    DocMappingBuilder mappingBuilder = new DocMappingBuilder(docStructure.doc(), docStore);
    desc.docStoreMapping(mappingBuilder, null);
    mappingBuilder.applyMapping();

    sortableMap = mappingBuilder.collectSortable();
    docStructure.prepareMany(desc);
    documentMapping = mappingBuilder.create(queueId, indexName, indexType);
    return documentMapping;
  }

  @Override
  public String indexType() {
    return indexType;
  }

  @Override
  public String indexName() {
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
    if (mapped && !registerPaths) {
      Collection<PathProperties.Props> pathProps = docStructure.doc().getPathProps();
      for (PathProperties.Props pathProp : pathProps) {
        String path = pathProp.getPath();
        if (path != null) {
          BeanDescriptor<?> targetDesc = desc.descriptor(path);
          BeanProperty idProperty = targetDesc.idProperty();
          if (idProperty != null) {
            // embedded beans don't have id property
            String fullPath = path + "." + idProperty.name();
            targetDesc.docStoreAdapter().registerInvalidationPath(desc.docStoreQueueId(), fullPath, pathProp.getProperties());
          }
        }
      }
      registerPaths = true;
    }
  }

  /**
   * Register a doc store invalidation listener for the given bean type, path and properties.
   */
  @Override
  public void registerInvalidationPath(String queueId, String path, Set<String> properties) {
    if (!mapped) {
      if (update == DocStoreMode.IGNORE) {
        // bean type not mapped but is included as nested document
        // in a doc store index so we need to update
        update = DocStoreMode.UPDATE;
      }
    }
    embeddedInvalidation.add(embeddedInvalidation(queueId, path, properties));
  }

  /**
   * Return the DsInvalidationListener based on the properties, path.
   */
  protected DocStoreEmbeddedInvalidation embeddedInvalidation(String queueId, String path, Set<String> properties) {
    if (properties.contains("*")) {
      return new DocStoreEmbeddedInvalidation(queueId, path);
    } else {
      return new DocStoreEmbeddedInvalidationProperties(queueId, path, propertyPositions(properties));
    }
  }

  /**
   * Return the property names as property index positions.
   */
  protected int[] propertyPositions(Set<String> properties) {
    List<Integer> posList = new ArrayList<>();
    for (String property : properties) {
      BeanProperty prop = desc.beanProperty(property);
      if (prop != null) {
        posList.add(prop.propertyIndex());
      }
    }
    int[] pos = new int[posList.size()];
    for (int i = 0; i < pos.length; i++) {
      pos[i] = posList.get(i);
    }
    return pos;
  }

  @Override
  public void updateEmbedded(PersistRequestBean<T> request, DocStoreUpdates docStoreUpdates) {
    for (DocStoreEmbeddedInvalidation anEmbeddedInvalidation : embeddedInvalidation) {
      anEmbeddedInvalidation.embeddedInvalidate(request, docStoreUpdates);
    }
  }

  /**
   * Return the pathProperties which defines the JSON document to index.
   * This can add derived/embedded/nested parts to the document.
   */
  protected DocStructure derivePathProperties(PathProperties pathProps) {
    boolean includeByDefault = (pathProps == null);
    if (pathProps == null) {
      pathProps = new PathProperties();
    }
    return docStructure(pathProps, includeByDefault);
  }

  protected DocStructure docStructure(PathProperties pathProps, final boolean includeByDefault) {
    final DocStructure docStructure = new DocStructure(pathProps);
    BeanProperty[] properties = desc.propertiesNonTransient();
    for (BeanProperty property : properties) {
      property.docStoreInclude(includeByDefault, docStructure);
    }

    InheritInfo inheritInfo = desc.inheritInfo();
    if (inheritInfo != null) {
      inheritInfo.visitChildren(inheritInfo1 -> {
        for (BeanProperty localProperty : inheritInfo1.localProperties()) {
          localProperty.docStoreInclude(includeByDefault, docStructure);
        }
      });
    }
    return docStructure;
  }

  @Override
  public FetchPath embedded(String path) {
    return docStructure.embedded(path);
  }

  @Override
  public FetchPath embeddedManyRoot(String path) {
    return docStructure.embeddedManyRoot(path);
  }

  @Override
  public boolean mapped() {
    return mapped;
  }

  @Override
  public String queueId() {
    return queueId;
  }

  @Override
  public DocStoreMode mode(PersistRequest.Type persistType, DocStoreMode txnMode) {
    if (txnMode == null) {
      return mode(persistType);
    } else if (txnMode == DocStoreMode.IGNORE) {
      return DocStoreMode.IGNORE;
    }
    return mapped ? txnMode : mode(persistType);
  }

  private DocStoreMode mode(PersistRequest.Type persistType) {
    switch (persistType) {
      case INSERT:
        return insert;
      case UPDATE:
        return update;
      case DELETE:
        return delete;
      default:
        return DocStoreMode.IGNORE;
    }
  }

  /**
   * Return the supplied value or default to the bean name lower case.
   */
  protected String derive(BeanType<?> desc, String suppliedValue) {
    return (suppliedValue != null && !suppliedValue.isEmpty()) ? suppliedValue : desc.name().toLowerCase();
  }

  @Override
  public abstract void deleteById(Object idValue, DocUpdateContext txn) throws IOException;

  @Override
  public abstract void index(Object idValue, T entityBean, DocUpdateContext txn) throws IOException;

  @Override
  public abstract void insert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void update(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  @Override
  public abstract void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocUpdateContext txn) throws IOException;

}
