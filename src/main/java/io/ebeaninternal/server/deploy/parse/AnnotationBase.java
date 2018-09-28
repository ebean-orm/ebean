package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Platform;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Provides some base methods for processing deployment annotations. All findAnnotation* methods
 * are capable to search for meta-annotations (annotation that has an other annotation)
 * <p>
 * <p>search algorithm for ONE annotation:</p>
 * <ul>
 * <li>Check if annotation is direct on the property</li>
 * <li>if not found: Check all annotations at the annotateElement
 * if they have the needed annotation as meta annotation</li>
 * <li>if not found: go up to super class and try again
 * (only findAnnotationRecursive)</li>
 * </ul>
 * DFS (Depth-First-Search) is used. The algorithm is the same as it is used in Spring-Framework,
 * as the code is taken from there.
 * <p>
 * <p>search algoritm for a Set&lt;Annotation&gt; works a litte bit different, as it does not stop
 * on the first match, but continues searching down to the last corner to find all annotations.</p>
 * <p>
 * <p>To prevent endless recursion, the search algoritm tracks all visited annotations</p>
 * <p>
 * <p>Supports also "java 1.6 repeatable containers" like{@link JoinColumn} / {@link JoinColumns}.</p>
 * <p>
 * <p>This means, searching for <code>JoinColumn</code> will find them also if they are inside a
 * <code>JoinColumn<b>s</b></code> annotation</p>
 */
public abstract class AnnotationBase {

  protected final DatabasePlatform databasePlatform;
  protected final Platform platform;
  protected final NamingConvention namingConvention;
  protected final DeployUtil util;

  protected AnnotationBase(DeployUtil util) {
    this.util = util;
    this.databasePlatform = util.getDbPlatform();
    this.platform = databasePlatform.getPlatform();
    this.namingConvention = util.getNamingConvention();
  }

  /**
   * read the deployment annotations.
   */
  public abstract void parse();

  /**
   * Checks string is null or empty .
   */
  protected boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  /**
   * Return the annotation for the property.
   * <p>
   * Looks first at the field and then at the getter method. It searches for meta-annotations, but not
   * recursively in the class hierarchy.
   * </p>
   * <p>
   * If a <code>repeatable</code> annotation class is specified and the annotation is platform
   * specific then the platform specific annotation is returned. Otherwise the first annotation
   * is returned. Note that you need no longer handle "java 1.6 repeatable containers"
   * like {@link JoinColumn} / {@link JoinColumns} yourself.
   * </p>
   * <p>
   */
  protected <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    T a = null;
    Field field = prop.getField();
    if (field != null) {
      a = AnnotationUtil.findAnnotation(field, annClass, platform);
    }
    if (a == null) {
      Method method = prop.getReadMethod();
      if (method != null) {
        a = AnnotationUtil.findAnnotation(method, annClass, platform);
      }
    }
    return a;
  }

  /**
   * Return all annotations for this property. Annotations are not filtered by platfrom and you'll get
   * really all annotations that are directly, indirectly or meta-present.
   */
  protected <T extends Annotation> Set<T> getAll(DeployBeanProperty prop, Class<T> annClass) {
    Set<T> ret = null;
    Field field = prop.getField();
    if (field != null) {
      ret = AnnotationUtil.findAnnotations(field, annClass);
    }
    Method method = prop.getReadMethod();
    if (method != null) {
      if (ret != null) {
        ret.addAll(AnnotationUtil.findAnnotations(method, annClass));
      } else {
        ret = AnnotationUtil.findAnnotations(method, annClass);
      }
    }
    return ret;
  }

  /**
   * Return the annotation for the property.
   * <p>
   * Looks first at the field and then at the getter method. then at class level.
   * (This is used for SequenceGenerator e.g.)
   * </p>
   */
  protected <T extends Annotation> T find(DeployBeanProperty prop, Class<T> annClass) {
    T a = get(prop, annClass);
    if (a == null) {
      a = AnnotationUtil.findAnnotation(prop.getOwningType(), annClass, platform);
    }
    return a;
  }
 
}
