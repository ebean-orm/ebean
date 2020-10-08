package io.ebean.querybean.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper to read the current/existing prefixed entity classes.
 * <p>
 * These are added back on partial compile.
 */
class ReadModuleInfo {

  private final ProcessingContext ctx;

  public ReadModuleInfo(ProcessingContext ctx) {
    this.ctx = ctx;
  }

  ModuleMeta read(Element element) {
    final List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
    for (AnnotationMirror mirror : mirrors) {
      final String name = mirror.getAnnotationType().asElement().toString();
      if (Constants.MODULEINFO.equals(name)) {
        List<String> entities = readEntities("entities", mirror);
        List<String> other = readEntities("other", mirror);
        return new ModuleMeta(entities, other);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<String> readEntities(String key, AnnotationMirror mirror) {
    final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
    final Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> entries = elementValues.entrySet();

    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries) {
      if (key.equals(entry.getKey().getSimpleName().toString())) {
        return readAttributes(entry.getValue());
      }
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  private List<String> readAttributes(AnnotationValue value) {
    final Object entitiesValue = value.getValue();
    if (entitiesValue != null) {
      try {
        List<String> vals = new ArrayList<>();
        List<AnnotationValue> coll = (List<AnnotationValue>) entitiesValue;
        for (AnnotationValue annotationValue : coll) {
          vals.add((String) annotationValue.getValue());
        }
        return vals;
      } catch (Exception e) {
        ctx.logError(null, "Error reading ModuleInfo annotation, err " + e);
      }
    }
    return Collections.emptyList();
  }
}
