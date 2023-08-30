package io.ebeaninternal.server.deploy.visitor;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

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
    BeanProperty idProp = desc.idProperty();
    if (idProp != null && !idProp.name().equals("_$IdClass$")) {
      visit(propertyVisitor, idProp);
    }
    BeanPropertyAssocOne<?> unidirectional = desc.unidirectional();
    if (unidirectional != null) {
      visit(propertyVisitor, unidirectional);
    }
    for (BeanProperty p : desc.propertiesNonTransient()) {
      if (p.isDDLColumn()) {
        visit(propertyVisitor, p);
      }
    }
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
        BeanProperty[] embProps = assocOne.properties();
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
      pv.visitScalar(p, true);
    }
  }

}
