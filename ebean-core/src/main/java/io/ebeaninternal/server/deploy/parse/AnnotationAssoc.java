package io.ebeaninternal.server.deploy.parse;

import io.ebean.config.BeanNotRegisteredException;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;

abstract class AnnotationAssoc extends AnnotationParser {

  final BeanDescriptorManager factory;

  AnnotationAssoc(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorManager factory) {
    super(info, readConfig);
    this.factory = factory;
  }

  void setTargetType(Class<?> targetType, DeployBeanPropertyAssoc<?> prop) {
    if (!targetType.equals(void.class)) {
      prop.setTargetType(targetType);
    }
  }

  void setBeanTable(DeployBeanPropertyAssoc<?> prop) {
    BeanTable assoc = getBeanTable(prop);
    if (assoc == null) {
      throw new BeanNotRegisteredException(errorMsgMissingBeanTable(prop.getTargetType(), prop.getFullBeanName()));
    }
    prop.setBeanTable(assoc);
  }

  BeanTable getBeanTable(DeployBeanPropertyAssoc<?> prop) {
    return factory.getBeanTable(prop.getTargetType());
  }

  private String errorMsgMissingBeanTable(Class<?> type, String from) {
    return "Error with association to [" + type + "] from [" + from + "]. Is " + type + " registered? See https://ebean.io/docs/trouble-shooting#not-registered";
  }

}
