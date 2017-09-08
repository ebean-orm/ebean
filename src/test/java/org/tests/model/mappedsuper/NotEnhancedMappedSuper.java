package org.tests.model.mappedsuper;

import javax.persistence.MappedSuperclass;
//import javax.persistence.Transient;

@MappedSuperclass
public class NotEnhancedMappedSuper {

  public static String SOMETHING = "Hello";

  private transient Long one;

//  @Transient
  private transient Long two;

  public Long getOne() {
    return one;
  }

  public void setOne(Long one) {
    this.one = one;
  }

  public Long getTwo() {
    return two;
  }

  public void setTwo(Long two) {
    this.two = two;
  }

}
