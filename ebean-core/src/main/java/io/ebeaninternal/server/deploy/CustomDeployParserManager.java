package io.ebeaninternal.server.deploy;

import java.util.List;

import io.ebean.plugin.CustomDeployParser;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

public class CustomDeployParserManager {

  private final List<CustomDeployParser> parsers;

  public CustomDeployParserManager(BootupClasses bootupClasses) {
    parsers = bootupClasses.getCustomDeployParsers();
  }

  public void parse(DeployBeanInfo<?> value) {
    for (CustomDeployParser parser : parsers) {
      parser.parse(value.getDescriptor(), value.getUtil().dbPlatform());
    }
  }

}
