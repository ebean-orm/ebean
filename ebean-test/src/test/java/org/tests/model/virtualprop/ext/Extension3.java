package org.tests.model.virtualprop.ext;

import io.ebean.annotation.Formula;
import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.VirtualBase;

import jakarta.persistence.OneToOne;

/**
 * This class will add the fields 'virtualExtendOne' and 'firstName' to 'VirtualBase' by EntityExtension
 */
@EntityExtension(VirtualBase.class)
public class Extension3 {

  public static String foo() {
    return "foo";
  }

  @OneToOne(mappedBy = "base")
  private VirtualExtendOne virtualExtendOne;

  @Formula(select = "concat('Your name is ', ${ta}.data)")
  private String firstName;

  public static Extension3 get(VirtualBase found) {
    throw new NotEnhancedException();
  }

  public VirtualExtendOne getVirtualExtendOne() {
    return virtualExtendOne;
  }

  public void setVirtualExtendOne(VirtualExtendOne virtualExtendOne) {
    this.virtualExtendOne = virtualExtendOne;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
}
