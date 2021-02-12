package io.ebeaninternal.server.grammer;

import io.ebean.FetchConfig;

/**
 * Parse the path that potentially is a FetchConfig definition.
 */
class ParseFetchConfig {

  /**
   * Parse the path that potentially is a FetchConfig definition.
   * <p>
   * Return the FetchConfig if it is and otherwise null.
   * </p>
   */
  static FetchConfig parse(String path) {

    if (path.startsWith("lazy")) {
      if (path.length() == 4) {
        return FetchConfig.ofLazy();
      } else if (path.charAt(4) == '(') {
        path = path.substring(5);
        int batchSize = parseBatchSize(path);
        return FetchConfig.ofLazy(batchSize);
      } else {
        return null;
      }
    }

    if (path.startsWith("query")) {
      if (path.length() == 5) {
        return FetchConfig.ofQuery();
      } else if (path.charAt(5) == '(') {
        path = path.substring(6);
        int batchSize = parseBatchSize(path);
        return FetchConfig.ofQuery(batchSize);
      } else {
        return null;
      }
    }

    return null;
  }

  private static int parseBatchSize(String path) {
    path = path.substring(0, path.length() - 1);
    return Integer.parseInt(path);
  }
}
