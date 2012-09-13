package com.avaje.tests.model.basic.xtra;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.avaje.ebean.TxType;
import com.avaje.ebean.annotation.Transactional;

public class DummyDao {

  @Transactional(type = TxType.REQUIRES_NEW)
  public void doSomething() {
    
    System.out.println("Hello World");
  }

  @Transactional
  // ebean transactional annotation, not spring transactional annotation
  public void addToObject(Long id, Double anotherNumber, List<Long> ids) throws EntityNotFoundException {
    // and more code
  }
  
}
