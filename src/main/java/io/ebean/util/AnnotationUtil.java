package io.ebean.util;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Annotation utility methods to find annotations recursively - taken from the spring framework.
 */
public class AnnotationUtil {

  private static Map<AnnotatedElement, AnnotationMeta> annotationMeta = new ConcurrentHashMap<>();

  private static final ConcurrentMap<Class<? extends Annotation>, Method> valueMethods = new ConcurrentHashMap<>();
  // only a non-null-marker the valueMethods - Cache
  private static final Method nullMethod = getNullMethod();

  /**
   * Caches all annotations for an annotated element.
   */
  static class AnnotationMeta {
    final Map<Class<? extends Annotation>, Set<Annotation>> annotations = new HashMap<>();

    AnnotationMeta(AnnotatedElement elem) {
      Annotation[] anns = elem.getAnnotations();
      Set<Annotation> visited = new HashSet<>();
      scanAnnotations(anns, visited);
      // seal the metadata
      for (Entry<Class<? extends Annotation>, Set<Annotation>> entry : annotations.entrySet()) {
        entry.setValue(Collections.unmodifiableSet(entry.getValue()));
      }
    }

    <A extends Annotation> void scanAnnotations(A[] anns, Set<Annotation> visited) {
      for (Annotation ann : anns) {
        if (visited.add(ann)) {
          if (!isInJavaLangAnnotationPackage(ann)) {
            Class<? extends Annotation> type = ann.annotationType();
            annotations.computeIfAbsent(type, k -> new LinkedHashSet<>()).add(ann);
            scanAnnotations(type.getAnnotations(), visited);

            Method method = valueMethods.computeIfAbsent(type, AnnotationUtil::getValueMethod);
            if (method != nullMethod) {
              try {
                @SuppressWarnings("unchecked")
                A[] repeatedAnns = (A[]) method.invoke(ann);
                scanAnnotations(repeatedAnns, visited);
              } catch (Exception e) { // catch all exceptions (thrown by invoke)
                throw new RuntimeException(e);
              }
            }
          }
        }
      }
    }

    @SuppressWarnings("unchecked")
    <A extends Annotation> Set<A> findAnnotations(Class<A> annotationType) {
      return (Set<A>) annotations.getOrDefault(annotationType, Collections.emptySet());
    }
    @SuppressWarnings("unchecked")
    <A extends Annotation>  A findAnnotation(Class<A> annotationType) {
      Set<Annotation> set =  annotations.get(annotationType);
      return set == null ? null : (A) set.iterator().next();
    }
  }

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

    return annotationMeta.computeIfAbsent(annotatedElement, AnnotationMeta::new).findAnnotation(annotationType);
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

    while (clazz != null && clazz != Object.class) {
      A ann = findAnnotation(clazz, annotationType);
      if (ann != null) {
        return ann;
      }
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
    Objects.requireNonNull(annotationType);
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
    ret.addAll(findAnnotations(clazz, annotationType));
    for (Class<?> iface : clazz.getInterfaces()) {
      if (!iface.getName().startsWith("java.lang.") && visitedInterfaces.add(iface)) {
        findMetaAnnotationsRecursive(iface, annotationType, ret, visited, visitedInterfaces);
      }
    }
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
    return annotationMeta.computeIfAbsent(annotatedElement, AnnotationMeta::new).findAnnotations(annotationType);
  }

  // caches for getRepeatableValueMethod
  private static Method getNullMethod() {
    try {
      return AnnotationUtil.class.getDeclaredMethod("getNullMethod");
    } catch (NoSuchMethodException e) {
      return null;
    }
  }



  private static Method getValueMethod( Class<? extends Annotation> annType) {
    try {
      Method method = annType.getMethod("value");
      if (method.getReturnType().isArray() &&
          Annotation.class.isAssignableFrom(method.getReturnType().getComponentType())) {
        return method;
      }
    } catch (NoSuchMethodException e) {
      // nop
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return nullMethod;
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
