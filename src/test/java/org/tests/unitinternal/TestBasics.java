package org.tests.unitinternal;

import org.junit.Assert;
import org.junit.Test;


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

    Assert.assertTrue((parentPath("banana") == null));
    Assert.assertTrue((parentPath("banana.apple").equals("banana")));
    Assert.assertTrue((parentPath("banana.apple.o").equals("banana.apple")));

  }
}
