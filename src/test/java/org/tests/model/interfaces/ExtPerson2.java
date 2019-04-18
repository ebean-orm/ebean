package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Table;

import io.ebean.annotation.EntityImplements;
import io.ebean.annotation.EntityOverride;

@Entity
@EntityOverride(priority = 20)
@EntityImplements(IExtPerson2.class)
@Table(name="person")
public class ExtPerson2 extends Person implements IExtPerson2 {

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
