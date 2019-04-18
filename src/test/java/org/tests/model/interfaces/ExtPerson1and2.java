package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Table;

import io.ebean.annotation.EntityImplements;
import io.ebean.annotation.EntityOverride;

@Entity()
@Table(name="person")
@EntityImplements(IExtPerson2.class)
@EntityOverride(priority = -30)
public class ExtPerson1and2 extends ExtPerson1 implements IExtPerson1, IExtPerson2 {

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
