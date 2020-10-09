package io.ebeaninternal.dbmigration.model.visitor;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.visitor.BeanPropertyVisitor;
import io.ebeaninternal.server.deploy.visitor.VisitProperties;

import java.util.List;

/**
 * Makes use of BeanVisitor and PropertyVisitor to navigate BeanDescriptors
 * and their properties.
 */
public class VisitAllUsing extends VisitProperties {

  private final BeanVisitor visitor;

  private final List<BeanDescriptor<?>> descriptors;

  /**
   * Visit all the descriptors for a given server.
   */
  public VisitAllUsing(BeanVisitor visitor, SpiEbeanServer server) {
    this(visitor, server.getBeanDescriptors());
  }

  /**
   * Visit all the descriptors in the list.
   */
  public VisitAllUsing(BeanVisitor visitor, List<BeanDescriptor<?>> descriptors) {
    this.visitor = visitor;
    this.descriptors = descriptors;
  }

  public void visitAllBeans() {
    for (BeanDescriptor<?> desc : descriptors) {
      if (desc.isBaseTable()) {
        visitBean(desc, visitor);
      }
    }
  }

  /**
   * Visit the bean using a visitor.
   */
  protected void visitBean(BeanDescriptor<?> desc, BeanVisitor visitor) {
    BeanPropertyVisitor propertyVisitor = visitor.visitBean(desc);
    if (propertyVisitor != null) {
      visitProperties(desc, propertyVisitor);
    }
  }

}
