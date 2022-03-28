package io.ebean;

import java.util.Collection;
import java.util.Map;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;

/**
 * Will be invoked by DB.visitSave. This can be used to pass all modified beans to a validator:
 * 
 * <pre>
 * {@code
 *   List<String> errors = new ArrayList<>();
 *   DB.visitSave(myBean, new PersistVisitor {
 *     visitBean(EntityBean bean) {
 *       String error = validate(bean);
 *       if (error != null) {
 *         errors.add(error);
 *       }
 *     }
 *     return this;
 *   });
 *   if (errors.isEmpty()) {
 *     DB.save(myBean); // you may also prompt, if user is sure to save
 *   } else {
 *     System.error.println("There are validation errors: " + errors);
 *   }
 * }
 * </pre>
 * 
 * Validators like Hibernate-Validator have the ability to check also complex object graphs. While Hibernate is doing this, ebean
 * may lazy load (unmodified) data and will build a too large object graph.<br>
 * Checking with "visitSave" only validates beans that are really loaded and have a valid cascade-type, so that they will get
 * saved.
 * 
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface PersistVisitor {

  /**
   * This bean is part of the persist graph and should be validated.
   * I.e. the property containing the bean is loaded and cascade-type is set to PERSIST (or ALL)
   * (Note: The bean is not necessarily dirty.)
   * 
   * @return: a new PersistVisitor (or this) that is handling the dependent graph or null to stop here in the graph
   */
  PersistVisitor visitBean(EntityBean bean);

  /**
   * This property is part of the persist graph can be used to return an individual visitor for this property.
   * 
   * @return: a new PersistVisitor (or this) that is handling the dependent graph or null to stop here in the graph
   */
  default PersistVisitor visitProperty(Property prop) {
    return this;
  };

  /**
   * This collection is part of the persist graph can be used to return an individual visitor for this collection.
   * 
   * @return: a new PersistVisitor (or this) that is handling the dependent graph or null to stop here in the graph
   */
  default PersistVisitor visitCollection(Collection<?> collection) {
    return this;
  };

  /**
   * This map is part of the persist graph can be used to return an individual visitor for this map.
   * 
   * @return: a new PersistVisitor (or this) that is handling the dependent graph or null to stop here in the graph
   */
  default PersistVisitor visitMap(Map<?, ?> map) {
    return this;
  };

  /**
   * Called, when all properties of the current bean are visited. VisitBean and visitEnd is always called in pairs.
   */
  default void visitEnd() {
  }

}
