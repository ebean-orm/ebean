package io.ebean.config.dbplatform.yugabyte;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;

public class YugabytePlaform extends PostgresPlatform {

  public YugabytePlaform() {
    super();
    this.platform = Platform.YUGABYTE;
  }
}
