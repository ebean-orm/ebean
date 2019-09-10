package io.ebeaninternal.dbmigration.model.visitor;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.InheritInfoVisitor;

import java.util.List;

/**
 * Makes use of BeanVisitor and PropertyVisitor to navigate BeanDescriptors
 * and their properties.
 */
public class VisitAllUsing {

  /**
   * Visit a single Descriptor using the given visitor.
   */
  public static void visitOne(BeanDescriptor<?> descriptor, BeanPropertyVisitor visitor) {

    new VisitAllUsing().visitProperties(descriptor, visitor);
  }

  protected final BeanVisitor visitor;

  protected final List<BeanDescriptor<?>> descriptors;

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

  private VisitAllUsing() {
    this.visitor = null;
    this.descriptors = null;
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

  private void visitProperties(BeanDescriptor<?> desc, BeanPropertyVisitor propertyVisitor) {
    BeanProperty idProp = desc.getIdProperty();
    if (idProp != null) {
      visit(propertyVisitor, idProp);
    }

    BeanPropertyAssocOne<?> unidirectional = desc.getUnidirectional();
    if (unidirectional != null) {
      visit(propertyVisitor, unidirectional);
    }

    BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
    for (BeanProperty p : propertiesNonTransient) {
      if (p.isDDLColumn()) {
        visit(propertyVisitor, p);
      }
    }

    visitInheritanceProperties(desc, propertyVisitor);
    propertyVisitor.visitEnd();
  }

  /**
   * Visit the property.
   */
  protected void visit(BeanPropertyVisitor pv, BeanProperty p) {

    if (p instanceof BeanPropertyAssocMany<?>) {
      // oneToMany or manyToMany
      pv.visitMany((BeanPropertyAssocMany<?>) p);

    } else if (p instanceof BeanPropertyAssocOne<?>) {
      BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) p;
      if (assocOne.isEmbedded()) {
        // Embedded bean
        pv.visitEmbedded(assocOne);
        BeanProperty[] embProps = assocOne.getProperties();
        for (BeanProperty embProp : embProps) {
          pv.visitEmbeddedScalar(embProp, assocOne);
        }

      } else if (assocOne.isOneToOneExported()) {
        // associated one exported
        pv.visitOneExported(assocOne);

      } else {
        // associated one imported
        pv.visitOneImported(assocOne);
      }

    } else {
      // simple scalar type
      pv.visitScalar(p);
    }
  }


  /**
   * Visit all the other inheritance properties that are not on the root.
   */
  protected void visitInheritanceProperties(BeanDescriptor<?> descriptor, BeanPropertyVisitor pv) {

    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && inheritInfo.isRoot()) {
      // add all properties on the children objects
      inheritInfo.visitChildren(new InheritChildVisitor(this, pv));
    }
  }


  /**
   * Helper used to visit all the inheritInfo/BeanDescriptor in
   * the inheritance hierarchy (to add their 'local' properties).
   */
  protected static class InheritChildVisitor implements InheritInfoVisitor {

    private final VisitAllUsing owner;
    private final BeanPropertyVisitor pv;

    protected InheritChildVisitor(VisitAllUsing owner, BeanPropertyVisitor pv) {
      this.owner = owner;
      this.pv = pv;
    }

    @Override
    public void visit(InheritInfo inheritInfo) {
      for (BeanProperty beanProperty : inheritInfo.desc().propertiesLocal()) {
        if (beanProperty.isDDLColumn()) {
          owner.visit(pv, beanProperty);
        }
      }
    }
  }
}
