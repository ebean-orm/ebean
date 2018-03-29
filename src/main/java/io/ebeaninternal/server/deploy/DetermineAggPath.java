package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

class DetermineAggPath {

  /**
   * Return the many path for the given aggregation formula.
   */
  static String manyPath(String aggregation, DeployBeanDescriptor<?> desc) {
    DetermineAggPath.Path path = paths(aggregation);
    if (path.length() == 1) {
      // a top level aggregation (so here we need to exclude Id property)
      return null;
    }
    return path.getManyPath(0, desc);
  }

  static Path paths(String aggregation) {
    String aggPath = path(aggregation);
    return new Path(aggPath.split("\\."), aggregation);
  }

  /**
   * Parse and return the full path for the aggregation.
   */
  static String path(String aggregation) {

    // aggregations always have a form of sum(), avg(), max(), count() etc
    // so find the first open bracket
    int start = aggregation.indexOf('(');
    if (start == -1) {
      throw new IllegalArgumentException("Aggregation formula [" + aggregation + "] is expected to have a '(' ?");
    }
    for (int i = start + 1; i < aggregation.length(); i++) {
      char ch = aggregation.charAt(i);
      if (!isNamePart(ch)) {
        return aggregation.substring(start + 1, i);
      }
    }

    throw new IllegalArgumentException("Could not find path in aggregation formula [" + aggregation + "]");
  }

  private static boolean isNamePart(char ch) {
    return ch == '.' || Character.isJavaIdentifierPart(ch);
  }


  /**
   * Helper class holding aggregation path segments.
   */
  static class Path {

    final String aggregation;
    final String[] paths;

    Path(String[] paths, String aggregation) {
      this.paths = paths;
      this.aggregation = aggregation;
    }

    int length() {
      return paths.length;
    }

    String path(int pos) {
      if (pos == 0) {
        return paths[0];
      } else {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pos; i++) {
          if (i > 0) {
            sb.append(".");
          }
          sb.append(paths[i]);
        }
        return sb.toString();
      }
    }

    String getManyPath(int pos, DeployBeanDescriptor<?> desc) {
      while (true) {

        String path = paths[pos];
        DeployBeanProperty details = desc.getBeanProperty(path);
        if (details instanceof DeployBeanPropertyAssocMany<?>) {
          return path(pos);

        } else if (details instanceof DeployBeanPropertyAssocOne<?>) {
          DeployBeanPropertyAssocOne<?> one = (DeployBeanPropertyAssocOne<?>) details;
          DeployBeanDescriptor<?> targetDesc = one.getTargetDeploy();
          desc = targetDesc;
          pos = pos + 1;
          continue;
        }
        throw new IllegalArgumentException("Can not find path to many in aggregation formula [" + aggregation + "]");
      }
    }
  }
}
