package io.ebean.platform.yugabyte;

import io.ebean.annotation.Platform;
import io.ebean.platform.postgres.PostgresPlatform;

public class YugabytePlatform extends PostgresPlatform {

  public YugabytePlatform() {
    super();
    this.platform = Platform.YUGABYTE;
  }
}
