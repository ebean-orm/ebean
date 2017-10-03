package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Platform;
import io.ebean.annotation.Formula;
import io.ebean.annotation.Where;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides some base methods for processing deployment annotations. All findAnnotation* methods
 * are capable to search for meta-annotations (annotation that has an other annotation)
 *
 * <p>search algorithm for ONE annotation:</p>
 * <ul>
 *  <li>Check if annotation is direct on the property</li>
 *  <li>if not found: Check all annotations at the annotateElement
 *  if they have the needed annotation as meta annotation</li>
 *  <li>if not found: go up to super class and try again
 *  (only findAnnotationRecursive)</li>
 * </ul>
 * DFS (Depth-First-Search) is used. The algorithm is the same as it is used in Spring-Framework,
 * as the code is taken from there.
 *
 * <p>search algoritm for a Set&lt;Annotation&gt; works a litte bit different, as it does not stop
 * on the first match, but continues searching down to the last corner to find all annotations.</p>
 *
 * <p>To prevent endless recursion, the search algoritm tracks all visited annotations</p>
 *
 * <p>Supports also "java 1.6 repeatable containers" like{@link JoinColumn} / {@link JoinColumns}.</p>
 *
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
   * specific(see {@link #getPlatformMatchingAnnotation(Set, Platform)}), then the platform specific
   * annotation is returned. Otherwise the first annotation is returned. Note that you need no longer
   * handle "java 1.6 repeatable containers" like {@link JoinColumn} / {@link JoinColumns} yourself.
   * </p>
   * <p>
   */
  protected <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    T a = null;
    Field field = prop.getField();
    if (field != null) {
      a = findAnnotation(field, annClass);
    }
    if (a == null) {
      Method method = prop.getReadMethod();
      if (method != null) {
        a = findAnnotation(method, annClass);
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
      ret = findAnnotations(field, annClass);
    }
    Method method = prop.getReadMethod();
    if (method != null) {
      if (ret != null) {
        ret.addAll(findAnnotations(method, annClass));
      } else {
        ret = findAnnotations(method, annClass);
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
      a = findAnnotation(prop.getOwningType(), annClass, platform);
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
   * It also does not filter out platform dependent annotations!
   */
  public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    // check if directly present, if not, start search for meta-annotations.
    A ann = annotatedElement.getAnnotation(annotationType);
    if (ann != null) {
      return ann;
    } else {
      return findAnnotation(annotatedElement, annotationType, new HashSet<>());
    }
  }

  /**
   * Find a single {@link Annotation} of {@code annotationType} on the supplied class.
   * <p>Meta-annotations will be searched if the annotation is not directly present on
   * the supplied element.
   * <p><strong>Note</strong>: this method searches for annotations at class & superClass(es)!
   */
  public static <A extends Annotation> A findAnnotationRecursive(Class<?> clazz, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    // check if directly present, if not, start search for meta-annotations.
    A ann = clazz.getAnnotation(annotationType);
    if (ann != null) {
      return ann;
    } else {
      while (clazz != null && clazz != Object.class) {
        ann = findAnnotation(clazz, annotationType, new HashSet<>());
        if (ann != null) {
          return ann;
        }
        // no meta-annotation present at this class - traverse to superclass
        clazz = clazz.getSuperclass();
      }
      return null;
    }
  }

  /**
   * Finds the first annotation of a type for this platform. (if annotation is platform specific, otherwise first
   * found annotation is returned)
   */
  public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Platform platform) {
    if (annotationType == null) {
      return null;
    }
    Set<A> anns = findAnnotations(annotatedElement, annotationType);
    return getPlatformMatchingAnnotation(anns, platform);
  }

  /**
   * Finds all annotations recusively for a class and its superclasses.
   */
  public static <A extends Annotation> Set<A> findAnnotationsRecursive(Class<?> clazz, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    Set<A> ret = new LinkedHashSet<>();
    Set<Annotation> visited = new HashSet<>();
    while (clazz != null && clazz != Object.class) {
      findMetaAnnotations(clazz, annotationType, ret, visited);
      clazz = clazz.getSuperclass();
    }
    return ret;
  }

  /**
   * Perform the search algorithm avoiding endless recursion by tracking which
   * annotations have already been visited.
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {

    Annotation[] anns = annotatedElement.getAnnotations(); // directly annotatated or inherited
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

  /**
   * Find all {@link Annotation}s of {@code annotationType} on the supplied {@link AnnotatedElement}.
   * <p>
   * Meta-annotations will be searched if the annotation is not <em>directly present</em> on the supplied element.
   * <p>
   * <strong>Warning</strong>: this method operates generically on annotated elements. In other words, this method
   * does not execute specialized search algorithms for classes or methods. It only traverses through Annotations!
   */
  public static <A extends Annotation> Set<A> findAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    Set<A> ret = new LinkedHashSet<>();
    findMetaAnnotations(annotatedElement, annotationType, ret, new HashSet<>());
    return ret;
  }

  /**
   * Perform the search algorithm avoiding endless recursion by tracking which
   * annotations have already been visited.
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> void findMetaAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType, Set<A> ret, Set<Annotation> visited) {

    Annotation[] anns = annotatedElement.getAnnotations();
    for (Annotation ann : anns) {
      if (!isInJavaLangAnnotationPackage(ann) && visited.add(ann)) {
        if (ann.annotationType() == annotationType) {
          ret.add((A) ann);
        } else {
          Method repeatableValueMethod = getRepeatableValueMethod(ann, annotationType);
          if (repeatableValueMethod != null) {
            try {
              A[] repeatedAnns = (A[]) repeatableValueMethod.invoke(ann);
              for (Annotation repeatedAnn : repeatedAnns) {
                ret.add((A) repeatedAnn);
                findMetaAnnotations(repeatedAnn.annotationType(), annotationType, ret, visited);
              }
            } catch (Exception e) { // catch all exceptions (thrown by invoke)
              throw new RuntimeException(e);
            }
          } else {
            findMetaAnnotations(ann.annotationType(), annotationType, ret, visited);
          }
        }
      }
    }
  }

  // caches for getRepeatableValueMethod
  private static Method getNullMethod() {
    try {
      return AnnotationBase.class.getDeclaredMethod("getNullMethod");
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static final ConcurrentMap<Annotation, Method> valueMethods = new ConcurrentHashMap<>();
  // only a non-null-marker the valueMethods - Cache
  private static final Method nullMethod = getNullMethod();


  /**
   * Returns the <code>value()</code> method for a possible containerAnnotation.
   * Method is retuned only, if its signature is <code>array of containingType</code>.
   */
  private static <A extends Annotation> Method getRepeatableValueMethod(
    Annotation containerAnnotation, Class<A> containingType) {

    Method method = valueMethods.get(containerAnnotation);
    if (method == null) {
      try {
        method = containerAnnotation.annotationType().getMethod("value");
      } catch (NoSuchMethodException e) {
        method = nullMethod;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      Method prev = valueMethods.putIfAbsent(containerAnnotation, method);
      method = prev == null ? method : prev;
    }
    if (method != nullMethod) {
      Class<?> retType = method.getReturnType();
      if (retType.isArray() && retType.getComponentType() == containingType) {
        return method;
      }
    }
    return null;
  }

  /**
   * Finds a suitable annotation from <code>Set<T> anns</code> for this platform.
   * To distinguish between platforms, annotation type <code>T</code> must define
   * a method withthis signature:
   * <p>
   * <code>Class<? extends DatabasePlatform>[] platforms() default {};</code>
   * </p>
   * The finding rules are:
   * <ol>
   * <li>Check if T has method "platforms" if not, return <code>ann[0]</code></code>
   * <li>find the annotation that is defined for <code>databasePlatform</code></li>
   * <li>otherwise return the annotation for default platform (platforms = {})</li>
   * <li>return null
   * </ol>
   * (This mechanism is currently used by {@link Where} and {@link Formula})
   */
  public static <T extends Annotation> T getPlatformMatchingAnnotation(Set<T> anns, Platform matchPlatform) {
    if (anns.isEmpty()) {
      return null;
    }
    Method getPlatformsMethod = null;
    T fallback = null;
    for (T ann : anns) {
      try {
        if (getPlatformsMethod == null) {
          getPlatformsMethod = ann.getClass().getMethod("platforms");
        }
        if (!Platform[].class.isAssignableFrom(getPlatformsMethod.getReturnType())) {
          return ann;
        }
        Platform[] platforms = (Platform[]) getPlatformsMethod.invoke(ann);
        if (platforms.length == 0) {
          fallback = ann;
        } else {
          for (Platform platform : platforms) {
            if (matchPlatform == platform) {
              return ann;
            }
          }
        }
      } catch (NoSuchMethodException e) {
        return ann; // not platform specific - return first one
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return fallback;
  }

}
