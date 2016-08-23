package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.annotation.Sql;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationSql extends AnnotationParser {

  public AnnotationSql(DeployBeanInfo<?> info, boolean javaxValidationAnnotations) {
    super(info, javaxValidationAnnotations);
  }

  public void parse() {
    Class<?> cls = descriptor.getBeanType();
    Sql sql = AnnotationBase.findAnnotation(cls,Sql.class);
    if (sql != null) {
      descriptor.setEntityType(BeanDescriptor.EntityType.SQL);
    }
  }

}
