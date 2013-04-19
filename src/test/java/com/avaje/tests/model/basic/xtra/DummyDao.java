package com.avaje.tests.model.basic.xtra;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxType;
import com.avaje.ebean.annotation.Transactional;

public class DummyDao {

  Logger logger = LoggerFactory.getLogger(DummyDao.class);
  
  @Transactional(type = TxType.REQUIRES_NEW)
  public void doSomething() {
    
    logger.info("  --- in DummyDao.doSomething() with TxType.REQUIRES_NEW");
    Transaction txn = Ebean.currentTransaction();
    if (txn == null) {
      logger.error("  NO TRANSACTION ??");
    } else {
      logger.info("  --- txn - "+txn);  
    }
    
  }

  @Transactional
  public void addToObject(Long id, Double anotherNumber, List<Long> ids) throws EntityNotFoundException {
    // and more code
  }
  
}
