package org.tests.model.virtualprop.ext;

import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.VirtualBaseA;

/**
 * This class will add the field 'ext' to 'VirtualBaseA' by EntityExtension
 */
@EntityExtension(VirtualBaseA.class)
public class Extension4 {

  private String extA;

  public String getExtA() {
    return extA;
  }

  public void setExtA(String extA) {
    this.extA = extA;
  }

  public static Extension4 get(VirtualBaseA base) {
    throw new NotEnhancedException();
  }
}
