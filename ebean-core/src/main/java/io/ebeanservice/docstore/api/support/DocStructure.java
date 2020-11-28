package io.ebeanservice.docstore.api.support;

import io.ebean.FetchPath;
import io.ebean.text.PathProperties;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Document structure for mapping to document store.
 */
public class DocStructure {

  /**
   * The full document structure.
   */
  private final PathProperties doc;

  /**
   * The embedded document structures by path.
   */
  private final Map<String, PathProperties> embedded = new HashMap<>();

  private final Map<String, PathProperties> manyRoot = new HashMap<>();

  /**
   * Create given an initial deployment doc mapping.
   */
  public DocStructure(PathProperties pathProps) {
    this.doc = pathProps;
  }

  /**
   * Add a property at the root level.
   */
  public void addProperty(String name) {
    doc.addToPath(null, name);
  }

  /**
   * Add an embedded property with it's document structure.
   */
  public void addNested(String path, PathProperties embeddedDoc) {
    doc.addNested(path, embeddedDoc);
    embedded.put(path, embeddedDoc);
  }

  /**
   * Return the document structure.
   */
  public PathProperties doc() {
    return doc;
  }

  /**
   * Return the document structure for an embedded path.
   */
  public FetchPath getEmbedded(String path) {
    return embedded.get(path);
  }

  public FetchPath getEmbeddedManyRoot(String path) {
    return manyRoot.get(path);
  }

  /**
   * For 'many' nested properties we need an additional root based graph to fetch and update.
   */
  public <T> void prepareMany(BeanDescriptor<T> desc) {
    Set<String> strings = embedded.keySet();
    for (String prop : strings) {
      BeanPropertyAssoc<?> embProp = (BeanPropertyAssoc<?>) desc.findProperty(prop);
      if (embProp.isMany()) {
        prepare(prop, embProp);
      }
    }
  }

  /**
   * Add a PathProperties for an embedded 'many' property (at the root level).
   */
  private void prepare(String prop, BeanPropertyAssoc<?> embProp) {

    BeanDescriptor<?> targetDesc = embProp.getTargetDescriptor();

    PathProperties manyRootPath = new PathProperties();
    manyRootPath.addToPath(null, targetDesc.getIdProperty().getName());
    manyRootPath.addNested(prop, embedded.get(prop));

    manyRoot.put(prop, manyRootPath);
  }
}
