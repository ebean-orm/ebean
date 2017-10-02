package io.ebean.bean;

/**
 * Visitor for collecting new/old values for a bean update.
 */
public interface BeanDiffVisitor {

  /**
   * Collect a new/old value pair.
   */
  void visit(int position, Object newVal, Object oldVal);

  /**
   * Start processing an associated bean.
   */
  void visitPush(int position);

  /**
   * Stop processing an associated bean.
   */
  void visitPop();
}
