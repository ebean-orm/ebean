package io.ebean.plugin;

import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * Fired after all beans are parsed. You may implement own parsers to handle custom annotations.
 * (See test case for example)
 *
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface CustomDeployParser {

  void parse(DeployBeanDescriptorMeta descriptor, DatabasePlatform databasePlatform);
}
