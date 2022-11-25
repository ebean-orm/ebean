package org.tests.model.virtualprop.ext;

import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.AbstractVirtualBase;
import org.tests.model.virtualprop.VirtualBase;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * This class will add the fields 'virtualExtendManyToManys' to 'AbstractVirtualBase' by EntityExtension
 */
@EntityExtension(AbstractVirtualBase.class)
public class Extension2 {

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
