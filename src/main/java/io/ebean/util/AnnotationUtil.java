package io.ebean.util;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Annotation utility methods to find annotations recursively - taken from the spring framework.
 */
public class AnnotationUtil {

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
  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    // check if directly present, if not, start search for meta-annotations.
    Annotation[] anns = annotatedElement.getAnnotations();
    if (anns.length == 0) {
      return null; // no annotations present, so searching for meta annotations not required
    }

    // As we need the anns array anyway, we iterate over this instead
    // of using annotatedElement.getAnnotation(...) which is synchronized internally
    for (Annotation ann : anns) {
      if (ann.annotationType() == annotationType) {
        return (A) ann;
      }
    }

    return findAnnotation(anns, annotationType, new HashSet<>());

  }

  /**
   * Find a single {@link Annotation} of {@code annotationType} on the supplied class.
   * <p>Meta-annotations will be searched if the annotation is not directly present on
   * the supplied element.
   * <p><strong>Note</strong>: this method searches for annotations at class & superClass(es)!
   */
  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A findAnnotationRecursive(Class<?> clazz, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }

    while (clazz != null && clazz != Object.class) {
      // check if directly present, if not, start search for meta-annotations.
      Annotation[] anns = clazz.getAnnotations();
      if (anns.length != 0) {
        for (Annotation ann : anns) {
          if (ann.annotationType() == annotationType) {
            return (A) ann;
          }
        }

        A ann = findAnnotation(anns, annotationType, new HashSet<>());
        if (ann != null) {
          return ann;
        }
      }
      // no meta-annotation present at this class - traverse to superclass
      clazz = clazz.getSuperclass();
    }
    return null;

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
   * Finds all annotations recusively for a class and its superclasses or interfaces.
   */
  public static <A extends Annotation> Set<A> findAnnotationsRecursive(Class<?> clazz, Class<A> annotationType) {
    if (annotationType == null) {
      return null;
    }
    Set<A> ret = new LinkedHashSet<>();
    Set<Annotation> visited = new HashSet<>();
    Set<Class<?>> visitedInterfaces = new HashSet<>();
    while (clazz != null && !clazz.getName().startsWith("java.lang.")) {
      findMetaAnnotationsRecursive(clazz, annotationType, ret, visited, visitedInterfaces);
      clazz = clazz.getSuperclass();
    }
    return ret;
  }

  /**
   * Searches the interfaces for annotations.
   */
  private static <A extends Annotation> void findMetaAnnotationsRecursive(Class<?> clazz,
      Class<A> annotationType, Set<A> ret,
      Set<Annotation> visited, Set<Class<?>> visitedInterfaces) {
    findMetaAnnotations(clazz, annotationType, ret, visited);
    for (Class<?> iface : clazz.getInterfaces()) {
      if (!iface.getName().startsWith("java.lang.") && visitedInterfaces.add(iface)) {
        findMetaAnnotationsRecursive(iface, annotationType, ret, visited, visitedInterfaces);
      }
    }
  }

  /**
   * Perform the search algorithm avoiding endless recursion by tracking which
   * annotations have already been visited.
   */
  @SuppressWarnings("unchecked")
  private static <A extends Annotation> A findAnnotation(Annotation[] anns, Class<A> annotationType, Set<Annotation> visited) {


    for (Annotation ann : anns) {
      if (!isInJavaLangAnnotationPackage(ann) && visited.add(ann)) {
        Annotation[] metaAnns = ann.annotationType().getAnnotations();
        for (Annotation metaAnn : metaAnns) {
          if (metaAnn.annotationType() == annotationType) {
            return (A) metaAnn;
          }
        }
        if (metaAnns.length > 0) {
          A annotation = findAnnotation(metaAnns, annotationType, visited);
          if (annotation != null) {
            return annotation;
          }
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
      return AnnotationUtil.class.getDeclaredMethod("getNullMethod");
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
