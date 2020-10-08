package io.ebeaninternal.api;

import io.ebean.annotation.Platform;
import io.ebean.util.StringHelper;

public class PlatformMatch {

  /**
   * Return true if the script platforms is a match/supported for the given platform.
   *
   * @param platform  The database platform we are generating/running DDL for
   * @param platforms The platforms (comma delimited) this script should run for
   */
  public static boolean matchPlatform(Platform platform, String platforms) {
    if (platforms == null || platforms.trim().isEmpty()) {
      return true;
    }

    // match on base platform name and platform name
    for (String name : StringHelper.splitNames(platforms)) {
      if (name.equalsIgnoreCase(platform.base().name()) || name.equalsIgnoreCase(platform.name())) {
        return true;
      }
    }
    return false;
  }
}
