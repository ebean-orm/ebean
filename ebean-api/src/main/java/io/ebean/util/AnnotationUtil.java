package io.ebean.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Annotation utility methods to find annotations.
 */
public class AnnotationUtil {

  /**
   * Determine if the supplied {@link Annotation} is defined in the core JDK {@code java.lang.annotation} package.
   */
  public static boolean notJavaLang(Annotation annotation) {
    return !annotation.annotationType().getName().startsWith("java.lang.annotation");
  }

  /**
   * Simple get on field or method with no meta-annotations or platform filtering.
   */
  public static <A extends Annotation> A get(AnnotatedElement element, Class<A> annotation) {
    return element.getAnnotation(annotation);
  }

  /**
   * Simple has with no meta-annotations or platform filtering.
   */
  public static <A extends Annotation> boolean has(AnnotatedElement element, Class<A> annotation) {
    return get(element, annotation) != null;
  }

  /**
   * On class get the annotation - includes inheritance.
   */
  public static <A extends Annotation> A typeGet(Class<?> clazz, Class<A> annotationType) {
    while (clazz != null && clazz != Object.class) {
      final A val = clazz.getAnnotation(annotationType);
      if (val != null) {
        return val;
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

  /**
   * On class get all the annotations - includes inheritance.
   */
  public static <A extends Annotation> Set<A> typeGetAll(Class<?> clazz, Class<A> annotationType) {
    Set<A> result = new LinkedHashSet<>();
    typeGetAllCollect(clazz, annotationType, result);
    return result;
  }

  private static <A extends Annotation> void typeGetAllCollect(Class<?> clazz, Class<A> annotationType, Set<A> result) {
    while (clazz != null && clazz != Object.class) {
      final A[] annotations = clazz.getAnnotationsByType(annotationType);
      Collections.addAll(result, annotations);
      clazz = clazz.getSuperclass();
    }
  }

  /**
   * On class simple check for annotation - includes inheritance.
   */
  public static <A extends Annotation> boolean typeHas(Class<?> clazz, Class<A> annotation) {
    return typeGet(clazz, annotation) != null;
  }

  /**
   * Check if an element is annotated with an annotation of given type searching meta-annotations.
   */
  public static boolean metaHas(AnnotatedElement element, Class<?> annotationType) {
    return !metaFindAll(element, annotationType).isEmpty();
  }

  /**
   * Find all the annotations of a given type searching meta-annotations.
   */
  public static Set<Annotation> metaFindAll(AnnotatedElement element, Class<?> annotationType) {
    return metaFindAllFor(element, Collections.singleton(annotationType));
  }

  /**
   * Find all the annotations for the filter searching meta-annotations.
   */
  public static Set<Annotation> metaFindAllFor(AnnotatedElement element, Set<Class<?>> filter) {
    Set<Annotation> visited = new HashSet<>();
    Set<Annotation> result = new LinkedHashSet<>();
    for (Annotation ann : element.getAnnotations()) {
      metaAdd(ann, filter, visited, result);
    }
    return result;
  }

  private static void metaAdd(Annotation ann, Set<Class<?>> filter, Set<Annotation> visited, Set<Annotation> result) {
    if (notJavaLang(ann) && visited.add(ann)) {
      if (filter.contains(ann.annotationType())) {
        result.add(ann);
      } else {
        for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
          metaAdd(metaAnn, filter, visited, result);
        }
      }
    }
  }
}
