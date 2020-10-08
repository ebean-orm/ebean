package io.ebean.querybean.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FindDbName {

  /**
   * Return the value of the DbName annotation or null if it isn't found on the element.
   */
  static String value(TypeElement element, Types typeUtils) {

    AnnotationMirror mirror = findDbNameMirror(element);
    if (mirror != null) {
      return readDbNameValue(mirror);
    }
    final TypeMirror typeMirror = element.getSuperclass();
    if (typeMirror.getKind() == TypeKind.NONE) {
      return null;
    }
    final TypeElement element1 = (TypeElement)typeUtils.asElement(typeMirror);
    return value(element1, typeUtils);
  }

  private static String readDbNameValue(AnnotationMirror mirror) {

    final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
    final Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> entries = elementValues.entrySet();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries) {
      if ("value".equals(entry.getKey().getSimpleName().toString())) {
        return (String) entry.getValue().getValue();
      }
    }
    return null;
  }

  private static AnnotationMirror findDbNameMirror(TypeElement element) {
    final List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
    for (AnnotationMirror mirror : mirrors) {
      final String name = mirror.getAnnotationType().asElement().toString();
      if (Constants.DBNAME.equals(name)) {
        return mirror;
      }
    }
    return null;
  }
}
