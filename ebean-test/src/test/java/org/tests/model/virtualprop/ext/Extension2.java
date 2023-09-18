package org.tests.model.virtualprop.ext;

import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.VirtualBase;

import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.List;

/**
 * This class will add the fields 'virtualExtendManyToManys' to 'AbstractVirtualBase' by EntityExtension
 */
@EntityExtension(VirtualBase.class)
public class Extension2 {

  public static String foo() {
    return "foo";
  }

  @ManyToMany
  @JoinTable(name = "kreuztabelle")
  private List<VirtualExtendManyToMany> virtualExtendManyToManys;

  public List<VirtualExtendManyToMany> getVirtualExtendManyToManys() {
    return virtualExtendManyToManys;
  }

  public static Extension2 get(VirtualBase found) {
    throw new NotEnhancedException();
  }
}
