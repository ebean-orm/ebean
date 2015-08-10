package com.avaje.ebean.dbmigration.model.visitor;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.InheritInfoVisitor;

import java.util.List;

/**
 * Makes use of BeanVisitor and PropertyVisitor to navigate BeanDescriptors
 * and their properties.
 */
public class VisitAllUsing {

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

  public void visitAllBeans() {
    for (BeanDescriptor<?> desc : descriptors) {
      if (desc.getBaseTable() != null) {
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

      BeanProperty idProp = desc.getIdProperty();
      if (idProp != null) {
        visit(propertyVisitor, idProp);
      }

      BeanPropertyAssocOne<?> unidirectional = desc.getUnidirectional();
      if (unidirectional != null) {
        visit(propertyVisitor, unidirectional);
      }

      BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
      for (int i = 0; i < propertiesNonTransient.length; i++) {
        BeanProperty p = propertiesNonTransient[i];
        if (!p.isFormula() && !p.isSecondaryTable()) {
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
        for (int i = 0; i < embProps.length; i++) {
          pv.visitEmbeddedScalar(embProps[i], assocOne);
        }

      } else if (assocOne.isOneToOneExported()) {
        // associated one exported
        pv.visitOneExported(assocOne);

      } else {
        // associated one imported
        pv.visitOneImported(assocOne);
      }

    } else if (p instanceof BeanPropertyCompound) {
      // compound type
      BeanPropertyCompound compound = (BeanPropertyCompound) p;
      pv.visitCompound(compound);

      BeanProperty[] properties = compound.getScalarProperties();
      for (int i = 0; i < properties.length; i++) {
        pv.visitCompoundScalar(compound, properties[i]);
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
      InheritChildVisitor childVisitor = new InheritChildVisitor(this, pv);
      inheritInfo.visitChildren(childVisitor);
    }
  }


  /**
   * Helper used to visit all the inheritInfo/BeanDescriptor in
   * the inheritance hierarchy (to add their 'local' properties).
   */
  protected class InheritChildVisitor implements InheritInfoVisitor {

    private final VisitAllUsing owner;
    private final BeanPropertyVisitor pv;

    protected InheritChildVisitor(VisitAllUsing owner, BeanPropertyVisitor pv) {
      this.owner = owner;
      this.pv = pv;
    }

    public void visit(InheritInfo inheritInfo) {
      BeanProperty[] propertiesLocal = inheritInfo.getBeanDescriptor().propertiesLocal();
      for (int i = 0; i <propertiesLocal.length ; i++) {
        owner.visit(pv, propertiesLocal[i]);
      }
    }
  }
}
