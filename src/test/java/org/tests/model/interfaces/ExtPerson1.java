package org.tests.model.interfaces;

import javax.persistence.Entity;

import io.ebean.annotation.EntityImplements;
import io.ebean.annotation.EntityOverride;

@Entity()
@EntityImplements(IExtPerson1.class)
@EntityOverride(priority = 30)
public class ExtPerson1 extends Person implements IExtPerson1 {

  private int myField1;

  @Override
  public int getMyField1() {
    return myField1;
  }

  @Override
  public void setMyField1(int myField1) {
    this.myField1 = myField1;
  }

}
