package org.tests.unitinternal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBasics {

  private String parentPath(String path) {
    int pos = path.lastIndexOf('.');
    if (pos == -1) {
      return null;
    } else {
      return path.substring(0, pos);
    }
  }

  @Test
  public void testParentPath() {
    assertTrue((parentPath("banana") == null));
    assertTrue((parentPath("banana.apple").equals("banana")));
    assertTrue((parentPath("banana.apple.o").equals("banana.apple")));
  }
}
