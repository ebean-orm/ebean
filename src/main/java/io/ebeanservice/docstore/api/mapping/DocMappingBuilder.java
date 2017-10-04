package io.ebeanservice.docstore.api.mapping;

import io.ebean.annotation.DocMapping;
import io.ebean.annotation.DocStore;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Builds the DocumentMapping for a given bean type.
 */
public class DocMappingBuilder {

  private final PathProperties paths;

  private final DocStore docStore;

  private final Stack<DocPropertyMapping> properties = new Stack<>();

  private final Map<String, DocPropertyMapping> map = new LinkedHashMap<>();

  /**
   * Create with the document structure paths and docStore deployment annotation.
   */
  public DocMappingBuilder(PathProperties paths, DocStore docStore) {
    this.paths = paths;
    this.docStore = docStore;
    this.properties.push(new DocPropertyMapping());
  }

  /**
   * Return true if the property is included in the document.
   */
  public boolean includesProperty(String prefix, String name) {
    return paths.includesProperty(prefix, name);
  }

  /**
   * Return true if the path is included in the document.
   */
  public boolean includesPath(String prefix, String name) {
    return paths.includesProperty(prefix, name);
  }

  /**
   * Add the property mapping.
   */
  public void add(DocPropertyMapping docMapping) {

    DocPropertyMapping currentParent = properties.peek();
    currentParent.addChild(docMapping);

    String parentName = currentParent.getName();
    String fullName = SplitName.add(parentName, docMapping.getName());
    map.put(fullName, docMapping);
  }

  /**
   * Push the nested object or list onto the properties stack.
   */
  public void push(DocPropertyMapping nested) {
    properties.push(nested);
  }

  /**
   * Pop the nested object or list off the properties stack.
   */
  public void pop() {
    properties.pop();
  }

  /**
   * Apply any override mappings from the top level docStore annotation.
   */
  public void applyMapping() {

    DocMapping[] mapping = docStore.mapping();
    for (DocMapping docMapping : mapping) {
      applyFieldMapping(null, docMapping);
    }
  }

  private void applyFieldMapping(String prefix, DocMapping docMapping) {

    String name = docMapping.name();
    String fullName = SplitName.add(prefix, name);

    DocPropertyMapping mapping = map.get(fullName);
    if (mapping == null) {
      throw new IllegalStateException("DocMapping for [" + fullName + "] but property not included in document?");
    }
    mapping.apply(docMapping);
  }

  /**
   * Collect the mapping of properties to 'raw' properties for those marked as sortable.
   */
  public Map<String, String> collectSortable() {

    DocPropertyMapping peek = properties.peek();
    SortableVisitor visitor = new SortableVisitor();
    peek.visit(visitor);

    return visitor.getSortableMap();
  }

  /**
   * Create the document mapping.
   */
  public DocumentMapping create(String queueId, String indexName, String indexType) {

    int shards = docStore.shards();
    int replicas = docStore.replicas();
    DocPropertyMapping root = properties.peek();
    return new DocumentMapping(queueId, indexName, indexType, paths, root, shards, replicas);
  }


  /**
   * Find sortable properties to build the mapping to 'raw' properties.
   */
  private static class SortableVisitor extends DocPropertyAdapter {

    private Map<String, String> sortableMap = new LinkedHashMap<>();

    @Override
    public void visitProperty(DocPropertyMapping property) {

      DocPropertyOptions options = property.getOptions();
      if (options != null && options.isSortable()) {
        String fullPath = pathStack.peekFullPath(property.getName());
        sortableMap.put(fullPath, fullPath + ".raw");
      }
    }

    private Map<String, String> getSortableMap() {
      return sortableMap;
    }
  }
}
