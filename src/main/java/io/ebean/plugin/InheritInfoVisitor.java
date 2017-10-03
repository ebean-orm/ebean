package io.ebean.plugin;

/**
 * Used to visit all the InheritInfo in a single inheritance hierarchy.
 */
public interface InheritInfoVisitor {

  /**
   * visit the InheritInfo for this node.
   */
  void visit(InheritInfo inheritInfo);

}
