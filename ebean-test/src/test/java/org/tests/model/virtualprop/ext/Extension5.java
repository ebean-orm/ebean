package org.tests.model.virtualprop.ext;

import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.VirtualBaseA;

import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will add the field 'ext' to 'VirtualBaseA' by EntityExtension
 */
@EntityExtension(VirtualBaseA.class)
public class Extension5 {

  @OneToMany
  private List<VirtualAExtendOne> virtualExtends = new ArrayList<>();

  public static Extension5 get(VirtualBaseA base) {
    throw new NotEnhancedException();
  }
}
