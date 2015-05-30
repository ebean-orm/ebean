package com.avaje.tests.basic.event;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.TWithPreInsert;

public class TestTransactionEvent extends TestCase {

  @Override
  protected void tearDown() throws Exception {
    MyTestTransactionEventListener.setDoTest(false);
  }

  @Override
  protected void setUp() throws Exception {
    MyTestTransactionEventListener.setDoTest(true);
  }

  public void test() {

    assertNull(MyTestTransactionEventListener.getLastCommitted());
    assertNull(MyTestTransactionEventListener.getLastRollbacked());

    final Object myUserObject = new Object();

    Transaction tx = Ebean.beginTransaction();
    tx.putUserObject("myUserObject", myUserObject);

    TWithPreInsert e = new TWithPreInsert();
    e.setTitle("Mister Transaction1");
    Ebean.save(e);

    tx.commit();

    assertNotNull(MyTestTransactionEventListener.getLastCommitted());
    assertNotNull(MyTestTransactionEventListener.getLastCommitted().getUserObject("myUserObject"));
    assertSame(MyTestTransactionEventListener.getLastCommitted().getUserObject("myUserObject"), myUserObject);
    assertNull(MyTestTransactionEventListener.getLastRollbacked());

    Transaction tx2 = Ebean.beginTransaction();
    tx2.putUserObject("myUserObject2", myUserObject);

    TWithPreInsert e2 = new TWithPreInsert();
    e2.setTitle("Mister Transaction2");
    Ebean.save(e2);

    tx2.rollback();

    assertNotNull(MyTestTransactionEventListener.getLastCommitted());
    assertNotNull(MyTestTransactionEventListener.getLastRollbacked());

    assertNotSame(MyTestTransactionEventListener.getLastCommitted(), MyTestTransactionEventListener.getLastRollbacked());

    assertNotNull(MyTestTransactionEventListener.getLastCommitted().getUserObject("myUserObject"));
    assertSame(MyTestTransactionEventListener.getLastCommitted().getUserObject("myUserObject"), myUserObject);
    
    assertNotNull(MyTestTransactionEventListener.getLastRollbacked().getUserObject("myUserObject2"));
    assertSame(MyTestTransactionEventListener.getLastRollbacked().getUserObject("myUserObject2"), myUserObject);
  }
}
