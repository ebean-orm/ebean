package org.tests.model.virtualprop.ext;

import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.AbstractVirtualBase;

/**
 * This class will add the field 'ext' to 'VirtualBaseA' by EntityExtension
 */
@EntityExtension(AbstractVirtualBase.class)
public class Extension1 {


  public static String foo() {
    return "foo";
  }

  private String ext;

  public String getExt() {
    return ext;
  }

  public void setExt(String ext) {
    this.ext = ext;
  }

  public static Extension1 get(AbstractVirtualBase base) {
    throw new NotEnhancedException();
  }
}
