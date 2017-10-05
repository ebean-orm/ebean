package io.ebean.plugin;

import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

public interface CustomDeployParser {

  void parse(DeployBeanInfo<?> beanInfo);
}
