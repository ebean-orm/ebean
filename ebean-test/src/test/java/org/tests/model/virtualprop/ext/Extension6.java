package org.tests.model.virtualprop.ext;

import io.ebean.annotation.DbJson;
import io.ebean.bean.NotEnhancedException;
import io.ebean.bean.extend.EntityExtension;
import org.tests.model.virtualprop.VirtualBaseA;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class will add the field 'nums' to 'VirtualBaseA' by EntityExtension
 */
@EntityExtension(VirtualBaseA.class)
public class Extension6 {

  @DbJson
  private Set<Integer> nums = new LinkedHashSet<>();

  public Set<Integer> getNums() {
    return nums;
  }

  public void setNums(Set<Integer> nums) {
    this.nums = nums;
  }

  public static Extension6 get(VirtualBaseA base) {
    throw new NotEnhancedException();
  }
}
