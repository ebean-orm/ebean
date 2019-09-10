package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Sql;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Read the class level deployment annotations.
 */
class AnnotationSql extends AnnotationParser {

  AnnotationSql(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info, readConfig);
  }

  @Override
  public void parse() {
    Class<?> cls = descriptor.getBeanType();
    Sql sql = AnnotationUtil.findAnnotationRecursive(cls, Sql.class);
    if (sql != null) {
      descriptor.setEntityType(BeanDescriptor.EntityType.SQL);
    }
  }

}
