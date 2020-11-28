package io.ebeaninternal.server.deploy.visitor;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.InheritInfoVisitor;

/**
 * Makes use of BeanVisitor and PropertyVisitor to navigate BeanDescriptors
 * and their properties.
 */
public class VisitProperties {

  /**
   * Visit a single Descriptor using the given visitor.
   */
  public static void visit(BeanDescriptor<?> descriptor, BeanPropertyVisitor visitor) {
    new VisitProperties().visitProperties(descriptor, visitor);
  }

  protected void visitProperties(BeanDescriptor<?> desc, BeanPropertyVisitor propertyVisitor) {
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

    private final VisitProperties owner;
    private final BeanPropertyVisitor pv;

    protected InheritChildVisitor(VisitProperties owner, BeanPropertyVisitor pv) {
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
