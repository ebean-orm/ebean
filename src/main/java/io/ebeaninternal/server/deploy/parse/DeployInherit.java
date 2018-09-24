package io.ebeaninternal.server.deploy.parse;

import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the InheritInfo deployment information.
 */
public class DeployInherit {

  private final Map<Class<?>, DeployInheritInfo> deployMap = new LinkedHashMap<>();

  private final Map<Class<?>, InheritInfo> finalMap = new LinkedHashMap<>();

  private final BootupClasses bootupClasses;

  /**
   * Create the InheritInfoDeploy.
   */
  public DeployInherit(BootupClasses bootupClasses) {
    this.bootupClasses = bootupClasses;
    initialise();
  }

  public void process(DeployBeanDescriptor<?> desc) {
    InheritInfo inheritInfo = finalMap.get(desc.getBeanType());
    desc.setInheritInfo(inheritInfo);
  }

  private void initialise() {
    List<Class<?>> entityList = bootupClasses.getEntities();

    findInheritClasses(entityList);
    buildDeployTree();
    buildFinalTree();
  }

  private void findInheritClasses(List<Class<?>> entityList) {

    // go through each class and initialise the info object...
    for (Class<?> cls : entityList) {
      if (isInheritanceClass(cls)) {
        DeployInheritInfo info = createInfo(cls);
        deployMap.put(cls, info);
      }
    }
  }

  private void buildDeployTree() {

    for (DeployInheritInfo info : deployMap.values()) {
      if (!info.isRoot()) {
        DeployInheritInfo parent = getInfo(info.getParent());
        parent.addChild(info);
      }
    }
  }

  private void buildFinalTree() {

    for (DeployInheritInfo deploy : deployMap.values()) {
      if (deploy.isRoot()) {
        // build tree top down...
        createFinalInfo(null, null, deploy);
      }
    }
  }

  private void createFinalInfo(InheritInfo root, InheritInfo parent, DeployInheritInfo deploy) {

    InheritInfo node = new InheritInfo(root, parent, deploy);
    if (parent != null) {
      parent.addChild(node);
    }
    finalMap.put(node.getType(), node);

    if (root == null) {
      root = node;
    }

    // buildFinalChildren(root, child, deploy);
    for (DeployInheritInfo childDeploy : deploy.children()) {
      createFinalInfo(root, node, childDeploy);
    }
  }

  /**
   * Build the InheritInfo for a given class.
   */
  private DeployInheritInfo getInfo(Class<?> cls) {
    return deployMap.get(cls);
  }

  private DeployInheritInfo createInfo(Class<?> cls) {

    DeployInheritInfo info = new DeployInheritInfo(cls);

    Class<?> parent = findParent(cls);
    if (parent != null) {
      info.setParent(parent);
    }

    Inheritance ia = AnnotationUtil.findAnnotationRecursive(cls, Inheritance.class);
    if (ia != null) {
      ia.strategy();
    }
    DiscriminatorColumn da = AnnotationUtil.findAnnotationRecursive(cls, DiscriminatorColumn.class);
    if (da != null) {
      // lowercase the discriminator column for RawSql and JSON
      info.setColumnName(da.name().toLowerCase());
      info.setColumnType(da.discriminatorType());
      info.setColumnLength(da.length());
      info.setColumnDefn(da.columnDefinition());
    }

    if (!info.isAbstract()) {
      DiscriminatorValue dv = AnnotationUtil.findAnnotation(cls, DiscriminatorValue.class); // do not search recursive
      if (dv != null) {
        info.setDiscriminatorValue(dv.value());
      } else {
        info.setDiscriminatorValue(cls.getSimpleName());
      }
    }
    return info;
  }

  private Class<?> findParent(Class<?> cls) {
    Class<?> superCls = cls.getSuperclass();
    if (isInheritanceClass(superCls)) {
      return superCls;
    } else {
      return null;
    }
  }

  private boolean isInheritanceClass(Class<?> cls) {
    while (true) {
      if (cls.equals(Object.class)) {
        return false;
      }
      Annotation a = AnnotationUtil.findAnnotationRecursive(cls, Inheritance.class);
      if (a != null) {
        return true;
      }
      // search up the inheritance heirarchy
      cls = cls.getSuperclass();
    }
  }

}
