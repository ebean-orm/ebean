package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides some base methods for processing deployment annotations.
 */
public abstract class AnnotationBase {

  protected final DatabasePlatform databasePlatform;
  protected final NamingConvention namingConvention;
  protected final DeployUtil util;

  protected AnnotationBase(DeployUtil util) {
    this.util = util;
    this.databasePlatform = util.getDbPlatform();
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
   * Looks first at the field and then at the getter method.
   * </p>
   */
  protected <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    T a = null;
    Field field = prop.getField();
    if (field != null) {
      a = findAnnotation(field, annClass);
    }
    if (a == null) {
      Method m = prop.getReadMethod();
      if (m != null) {
        a = findAnnotation(m, annClass);
      }
    }
    return a;
  }

  /**
   * Return the annotation for the property.
   * <p>
   * Looks first at the field and then at the getter method. then at class level.
   * </p>
   */
  protected <T extends Annotation> T find(DeployBeanProperty prop, Class<T> annClass) {
    T a = get(prop, annClass);
    if (a == null) {
      a = findAnnotation(prop.getOwningType(), annClass);
    }
    return a;
  }


  // this code is taken from the spring framework to find annotations recursively

  /**
   * Determine if the supplied {@link Annotation} is defined in the core JDK {@code java.lang.annotation} package.
   */
  public static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
    return annotation.annotationType().getName().startsWith("java.lang.annotation");
  }

  /**
   * Find a single {@link Annotation} of {@code annotationType} on the supplied {@link AnnotatedElement}.
   * <p>
   * Meta-annotations will be searched if the annotation is not <em>directly present</em> on the supplied element.
   * <p>
   * <strong>Warning</strong>: this method operates generically on annotated elements. In other words, this method
   * does not execute specialized search algorithms for classes or methods. It only traverses through Annotations!
   */
  public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    // check if directly present, if not, start recursive traversal
    A ann = annotatedElement.getAnnotation(annotationType);
    if (ann != null) {
      return ann;
    } else {
      return findAnnotation(annotatedElement, annotationType, new HashSet<Annotation>());
    }
  }

  /**
   * Find a single {@link Annotation} of {@code annotationType} on the supplied class.
   * <p>Meta-annotations will be searched if the annotation is not directly present on
   * the supplied element.
   * <p><strong>Note</strong>: this method searches for annotations at class & superClass(es)!
   */
  public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    // check if directly present, if not, start recursive traversal
    A ann = clazz.getAnnotation(annotationType);
    if (ann != null) {
      return ann;
    } else {
      while (clazz != null && clazz != Object.class) {
        ann = findAnnotation(clazz, annotationType, new HashSet<Annotation>());
        if (ann != null) {
          return ann;
        }
        // not present at this class - traverse to superclass
        clazz = clazz.getSuperclass();
      }
      return null;
    }
  }

  /**
   * Perform the search algorithm avoiding endless recursion by tracking which
   * annotations have already been visited.
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {

    Annotation[] anns = annotatedElement.getDeclaredAnnotations();
    for (Annotation ann : anns) {
      if (ann.annotationType() == annotationType) {
        return (A) ann;
      }
    }
    for (Annotation ann : anns) {
      if (!isInJavaLangAnnotationPackage(ann) && visited.add(ann)) {
        A annotation = findAnnotation(ann.annotationType(), annotationType, visited);
        if (annotation != null) {
          return annotation;
        }
      }
    }
    return null;
  }
}
