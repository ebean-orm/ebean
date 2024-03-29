package io.ebeanservice.docstore.api.mapping;

import io.ebean.annotation.DocMapping;
import io.ebean.core.type.DocPropertyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Property mapping in a doc store document structure.
 */
public final class DocPropertyMapping {

  private String name;
  private DocPropertyType type;
  private DocPropertyOptions options;
  private final List<DocPropertyMapping> children = new ArrayList<>();

  /**
   * Construct ROOT.
   */
  public DocPropertyMapping() {
    this.type = DocPropertyType.ROOT;
  }

  /**
   * Construct property mapping.
   */
  public DocPropertyMapping(String name, DocPropertyType type) {
    this.type = type;
    this.name = name;
    this.options = new DocPropertyOptions();
  }

  /**
   * Construct property mapping with options.
   */
  public DocPropertyMapping(String name, DocPropertyType type, DocPropertyOptions options) {
    this.name = name;
    this.type = type;
    this.options = options;
  }

  /**
   * Visit this property and any nested children.
   */
  public void visit(DocPropertyVisitor visitor) {
    switch (type) {
      case ROOT:
        visitor.visitBegin();
        visitChildren(visitor);
        visitor.visitEnd();
        break;
      case OBJECT:
        visitor.visitBeginObject(this);
        visitChildren(visitor);
        visitor.visitEndObject(this);
        break;
      case LIST:
        visitor.visitBeginList(this);
        visitChildren(visitor);
        visitor.visitEndList(this);
        break;
      default:
        visitor.visitProperty(this);
    }
  }

  private void visitChildren(DocPropertyVisitor visitor) {
    for (DocPropertyMapping property : children) {
      property.visit(visitor);
    }
  }

  @Override
  public String toString() {
    return "name:" + name + " type:" + type + " options:" + options;
  }

  /**
   * Return the type of the property.
   */
  public DocPropertyType type() {
    return type;
  }

  /**
   * Set the type of the property.
   */
  public void type(DocPropertyType type) {
    this.type = type;
  }

  /**
   * Return the property name.
   */
  public String name() {
    return name;
  }

  /**
   * Return the property options.
   */
  public DocPropertyOptions options() {
    return options;
  }

  /**
   * Return the child nested properties.
   */
  public List<DocPropertyMapping> children() {
    return children;
  }

  /**
   * Add a child property.
   */
  public void addChild(DocPropertyMapping docMapping) {
    children.add(docMapping);
  }

  /**
   * Apply mapping options to this property.
   */
  public void apply(DocMapping docMapping) {
    options.apply(docMapping);
  }
}
