package io.ebeanservice.docstore.api.mapping;

/**
 * Used to visit the properties in a document structure.
 */
public interface DocPropertyVisitor {

  /**
   * Begin visiting the document structure.
   */
  void visitBegin();

  /**
   * Visit a property.
   */
  void visitProperty(DocPropertyMapping property);

  /**
   * Start visiting a nested object.
   */
  void visitBeginObject(DocPropertyMapping property);

  /**
   * End visiting a nested object.
   */
  void visitEndObject(DocPropertyMapping property);

  /**
   * Start visiting a nested list.
   */
  void visitBeginList(DocPropertyMapping property);

  /**
   * End visiting a nested list.
   */
  void visitEndList(DocPropertyMapping property);

  /**
   * Finished visiting the document structure.
   */
  void visitEnd();

}
