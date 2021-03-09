package io.ebeaninternal.server.deploy.parse;

import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import java.util.Collections;
import java.util.List;

/**
 * Jakarta validation annotations reader.
 */
class ReadValidationAnnotationsJakarta implements ReadValidationAnnotations {

  ReadValidationAnnotationsJakarta(ReadAnnotationConfig readConfig) {
    readConfig.addMetaAnnotation(Size.class);
    readConfig.addMetaAnnotation(Size.List.class);
  }

  @Override
  public boolean isValidationNotNull(DeployBeanProperty property) {
    NotNull notNull = AnnotationUtil.get(property.getField(), NotNull.class);
    return (notNull != null && isEbeanValidationGroups(notNull.groups()));
  }

  private boolean isEbeanValidationGroups(Class<?>[] groups) {
    return groups.length == 0 || groups.length == 1 && Default.class.isAssignableFrom(groups[0]);
  }

  @Override
  public int maxSize(DeployBeanProperty property) {
    int maxSize = 0;
    for (Size size : getMetaAnnotationJavaxSize(property)) {
      if (size.max() < Integer.MAX_VALUE) {
        maxSize = Math.max(maxSize, size.max());
      }
    }
    return maxSize;
  }

  private List<Size> getMetaAnnotationJavaxSize(DeployBeanProperty prop) {
    final List<Size> size = prop.getMetaAnnotations(Size.class);
    final List<Size.List> lists = prop.getMetaAnnotations(Size.List.class);
    for (Size.List list : lists) {
      Collections.addAll(size, list.value());
    }
    return size;
  }
}
