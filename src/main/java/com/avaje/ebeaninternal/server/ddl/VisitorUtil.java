package com.avaje.ebeaninternal.server.ddl;

import java.util.List;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;

/**
 * Utility object to use BeanVisitor.
 */
public class VisitorUtil {

	/**
	 * Visit all the descriptors for a given server.
	 */
	public static void visit(SpiEbeanServer server, BeanVisitor visitor){

		visit(server.getBeanDescriptors(), visitor);
	}


	/**
	 * Visit all the descriptors in the list.
	 */
	public static void visit(List<BeanDescriptor<?>> descriptors, BeanVisitor visitor){

		visitor.visitBegin();

		for (BeanDescriptor<?> desc : descriptors) {

			if (desc.getBaseTable() != null){
				visitBean(desc, visitor);
			} 
		}

		visitor.visitEnd();
	}

	/**
	 * Visit the bean using a visitor.
	 */
  public static void visitBean(BeanDescriptor<?> desc, BeanVisitor visitor) {

    if (visitor.visitBean(desc)) {

      BeanProperty idProp = desc.getIdProperty();
      if (idProp != null) {
        visit(visitor, idProp);
      }
      BeanPropertyAssocOne<?> unidirectional = desc.getUnidirectional();
      if (unidirectional != null) {
        visit(visitor, unidirectional);
      }
      BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
      for (int i = 0; i < propertiesNonTransient.length; i++) {
        BeanProperty p = propertiesNonTransient[i];
        if (!p.isFormula() && !p.isSecondaryTable()) {
          visit(visitor, p);
        }
      }

      visitor.visitBeanEnd(desc);
    }
  }
	
  private static void visit(BeanVisitor visitor, BeanProperty p) {
    PropertyVisitor pv = visitor.visitProperty(p);
    if (pv != null) {
      visit(p, pv);
    }
  }

	/**
	 * Visit all the properties.
	 */
	public static void visit(BeanProperty[] p, PropertyVisitor pv){

		for (int i = 0; i < p.length; i++) {
			visit(p[i], pv);
		}
	}

	/**
	 * Visit the property.
	 */
	public static void visit(BeanProperty p, PropertyVisitor pv) {

		if (p instanceof BeanPropertyAssocMany<?>){
			pv.visitMany((BeanPropertyAssocMany<?>)p);

		} else if (p instanceof BeanPropertyAssocOne<?>){
			BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>)p;
			if (assocOne.isEmbedded()){
				pv.visitEmbedded(assocOne);
				BeanProperty[] embProps = assocOne.getProperties();
				for (int i = 0; i < embProps.length; i++) {
					pv.visitEmbeddedScalar(embProps[i], assocOne);
				}


			} else if (assocOne.isOneToOneExported()){
				pv.visitOneExported(assocOne);

			} else {
				pv.visitOneImported(assocOne);

			}
        } else if (p instanceof BeanPropertyCompound){
            BeanPropertyCompound compound = (BeanPropertyCompound)p;
            pv.visitCompound(compound);
            
            BeanProperty[] properties = compound.getScalarProperties();
            for (int i = 0; i < properties.length; i++) {
                pv.visitCompoundScalar(compound, properties[i]);
            }
            
		} else {
			pv.visitScalar(p);
		}
	}
}
