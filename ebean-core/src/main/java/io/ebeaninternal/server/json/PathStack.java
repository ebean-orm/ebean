package io.ebeaninternal.server.json;

import io.ebeaninternal.server.util.ArrayStack;

public final class PathStack extends ArrayStack<String> {

  public String peekFullPath(String key) {
    String prefix = peekWithNull();
    if (prefix != null) {
      return prefix + "." + key;
    } else {
      return key;
    }
  }

  public void pushPathKey(String key) {
    String prefix = peekWithNull();
    if (prefix != null) {
      key = prefix + "." + key;
    }
    push(key);
  }

}
