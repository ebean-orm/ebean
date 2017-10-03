package io.ebean.dbmigration.model.visitor;

import io.ebean.EbeanServer;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.InheritInfo;
import io.ebean.plugin.InheritInfoVisitor;
import io.ebean.plugin.Property;
import io.ebean.plugin.PropertyAssocMany;
import io.ebean.plugin.PropertyAssocOne;

import java.util.List;

/**
 * Makes use of BeanVisitor and PropertyVisitor to navigate BeanDescriptors
 * and their properties.
 */
public class VisitAllUsing {

  protected final BeanVisitor visitor;

  protected final List<? extends BeanType<?>> descriptors;

  /**
   * Visit all the descriptors for a given server.
   */
  public VisitAllUsing(BeanVisitor visitor, EbeanServer server) {

    this(visitor, server.getPluginApi().getBeanTypes());
  }

  /**
   * Visit all the descriptors in the list.
   */
  public VisitAllUsing(BeanVisitor visitor, List<? extends BeanType<?>> descriptors) {
    this.visitor = visitor;
    this.descriptors = descriptors;
  }

  public void visitAllBeans() {
    for (BeanType<?> desc : descriptors) {
      if (desc.isBaseTable()) {
        visitBean(desc, visitor);
      }
    }
  }

  /**
   * Visit the bean using a visitor.
   */
  protected void visitBean(BeanType<?> desc, BeanVisitor visitor) {

    BeanPropertyVisitor propertyVisitor = visitor.visitBean(desc);
    if (propertyVisitor != null) {

      Property idProp = desc.getIdProperty();
      if (idProp != null) {
        visit(propertyVisitor, idProp);
      }

      PropertyAssocOne unidirectional = desc.getUnidirectional();
      if (unidirectional != null) {
        visit(propertyVisitor, unidirectional);
      }

      Property[] propertiesNonTransient = desc.propertiesNonTransient();
      for (Property p : propertiesNonTransient) {
        if (p.isDDLColumn()) {
          visit(propertyVisitor, p);
        }
      }

      visitInheritanceProperties(desc, propertyVisitor);
      propertyVisitor.visitEnd();
    }
  }

  /**
   * Visit the property.
   */
  protected void visit(BeanPropertyVisitor pv, Property p) {

    if (p instanceof PropertyAssocMany) {
      // oneToMany or manyToMany
      pv.visitMany((PropertyAssocMany) p);

    } else if (p instanceof PropertyAssocOne) {
      PropertyAssocOne assocOne = (PropertyAssocOne) p;
      if (assocOne.isEmbedded()) {
        // Embedded bean
        pv.visitEmbedded(assocOne);
        Property[] embProps = assocOne.getProperties();
        for (Property embProp : embProps) {
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
  protected void visitInheritanceProperties(BeanType<?> descriptor, BeanPropertyVisitor pv) {

    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && inheritInfo.isRoot()) {
      // add all properties on the children objects
      InheritChildVisitor childVisitor = new InheritChildVisitor(this, pv);
      inheritInfo.visitChildren(childVisitor);
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
      for(Property prop : inheritInfo.getPropertiesLocal()) {
        owner.visit(pv, prop);
      }
    }
  }
}
