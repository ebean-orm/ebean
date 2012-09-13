package com.avaje.tests.unitinternal;

import javax.persistence.OptimisticLockException;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.EBasicVer;
import com.avaje.tests.model.basic.xtra.DummyDao;
import com.avaje.tests.model.basic.xtra.OptimisticLockExceptionThrowingDao;

public class TestTxTypeOnTransactional extends TestCase {

    public void test() {
        
        DummyDao dao = new DummyDao();   
        dao.doSomething();
    }
    
    public void testOptimisticException() {
        
        EBasicVer v = new EBasicVer();
        v.setName("occ");
        v.setDescription("blah");
        Ebean.save(v);
        
        
        OptimisticLockExceptionThrowingDao dao = new OptimisticLockExceptionThrowingDao();
        try {
            dao.doSomething(v);
            // never get here
            Assert.assertTrue(false);
        } catch (OptimisticLockException e){
            Transaction inMethodTransaction = dao.getInMethodTransaction();
            boolean active = inMethodTransaction.isActive();
            Assert.assertFalse(active);
        }
    }
    
}
