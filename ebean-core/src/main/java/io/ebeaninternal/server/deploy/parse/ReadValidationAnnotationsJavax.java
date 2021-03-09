package io.ebeaninternal.server.deploy.parse;

import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.util.Collections;
import java.util.List;

/**
 * Javax validation annotations reader.
 */
class ReadValidationAnnotationsJavax implements ReadValidationAnnotations {

  ReadValidationAnnotationsJavax(ReadAnnotationConfig readConfig) {
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
  public int maxSize(DeployBeanProperty prop) {
    int maxSize = 0;
    for (Size size : getMetaAnnotationJavaxSize(prop)) {
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
