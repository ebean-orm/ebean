package org.tests.model.interfaces;

import io.ebean.annotation.ext.EntityImplements;
import io.ebean.annotation.ext.EntityOverride;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity()
@Table(name = "person")
@EntityImplements(IExtPerson2.class)
@EntityOverride(priority = -30)
public class ExtPerson1and2 extends ExtPerson1 implements IExtPerson2 {

  private int myField2;

  @Override
  public int getMyField2() {
    return myField2;
  }

  @Override
  public void setMyField2(int myField2) {
    this.myField2 = myField2;
  }
}
