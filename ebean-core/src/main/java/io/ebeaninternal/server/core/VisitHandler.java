package io.ebeaninternal.server.core;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import io.ebean.PersistVisitor;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Handler to process persist graphs. It will allow you to visit a persist
 * action (insert/update) and get information about all beans that will be
 * affected by that action. This allows you to do validation and other things on
 * a set of beans.
 * 
 * @author Roland Praml, FOCONIS AG
 *
 */
class VisitHandler {

  private final Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
  private final BeanDescriptorManager beanDescriptorManager;

  public VisitHandler(BeanDescriptorManager beanDescriptorManager) {
    this.beanDescriptorManager = beanDescriptorManager;
  }

  public void visit(Object start, PersistVisitor visitor) {
    if (start != null) {
      if (start instanceof EntityBean) {
        visitBean((EntityBean) start, visitor);
      } else {
        visitMany(start, visitor);
      }
    }
    visitor.visitEnd();
  }

  private void visitBean(EntityBean bean, PersistVisitor visitor) {
    if (!seen.add(bean)) {
      return;
    }
    visitor = visitor.visitBean(bean);
    if (visitor != null) {
      BeanDescriptor<?> desc = beanDescriptorManager.descriptor(bean.getClass());
      visitOnes(bean, visitor, desc, desc.propertiesOneImportedSave());
      visitOnes(bean, visitor, desc, desc.propertiesOneExportedSave());
      visitManys(bean, visitor, desc, desc.propertiesManySave());
      visitor.visitEnd();
    }
  }

  private void visitManys(EntityBean bean, PersistVisitor visitor, BeanDescriptor<?> desc,
      BeanPropertyAssocMany<?>[] manys) {
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    for (BeanPropertyAssocMany<?> many : manys) {
      // check that property is loaded and collection should be cascaded to
      if (ebi.isLoadedProperty(many.propertyIndex()) && !many.isSkipSaveBeanCollection(bean, ebi.isNew())) {
        Object manyValue = many.getValue(bean);
        if (manyValue != null) {
          PersistVisitor propertyVisitor = visitor.visitProperty(many);
          if (propertyVisitor != null) {
            visitMany(manyValue, propertyVisitor);
            propertyVisitor.visitEnd();
          }
        }
      }
    }
  }

  private void visitMany(Object many, PersistVisitor visitor) {
    if (!seen.add(many)) {
      return;
    }
    if (many instanceof Collection) {
      Collection<?> coll = (Collection<?>) many;
      PersistVisitor collectionVisitor = visitor.visitCollection(coll);
      if (collectionVisitor != null) {
        for (Object elem : coll) {
          visitBean((EntityBean) elem, collectionVisitor);
        }
        collectionVisitor.visitEnd();
      }
    } else if (many instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) many;
      PersistVisitor mapVisitor = visitor.visitMap(map);
      if (mapVisitor != null) {
        for (Object elem : ((Map<?, ?>) many).values()) {
          visitBean((EntityBean) elem, mapVisitor);
        }
        mapVisitor.visitEnd();
      }
    } else {
      throw new IllegalArgumentException("Object " + many + " cannot be visited in persist graph");
    }
  }

  private void visitOnes(EntityBean bean, PersistVisitor visitor, BeanDescriptor<?> desc,
      BeanPropertyAssocOne<?>[] ones) {
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    for (BeanPropertyAssocOne<?> prop : ones) {
      if (ebi.isLoadedProperty(prop.propertyIndex())) {
        EntityBean detailBean = prop.getValueAsEntityBean(bean);
        if (detailBean != null && !prop.isSaveRecurseSkippable(detailBean) && !prop.isReference(detailBean)) {
          PersistVisitor propertyVisitor = visitor.visitProperty(prop);
          if (propertyVisitor != null) {
            visitBean(detailBean, propertyVisitor);
            propertyVisitor.visitEnd();
          }
        }
      }
    }
  }
}