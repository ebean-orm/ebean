package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.sql.Types;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestTransient extends BaseTestCase {

  @Test
  public void testTransient() {

    Customer cnew = new Customer();
    cnew.setName("testTrans");

    DB.save(cnew);
    Integer custId = cnew.getId();

    Customer c = DB.find(Customer.class).setId(custId).findOne();

    assertNotNull(c);

    BeanState beanState = DB.getBeanState(c);
    assertFalse("not new or dirty as transient", beanState.isNewOrDirty());

    c.getLock().tryLock();
    try {
      c.setSelected(Boolean.TRUE);
    } finally {
      c.getLock().unlock();
    }

    Boolean selected = c.getSelected();
    assertNotNull(selected);

    assertFalse("not new or dirty as transient", beanState.isNewOrDirty());

    DB.save(c);

    selected = c.getSelected();
    assertNotNull(selected);

    c.setName("Modified");
    assertTrue("dirty now", beanState.isNewOrDirty());

    selected = c.getSelected();
    assertNotNull(selected);

    DB.save(c);
    assertFalse("Not dirty after save", beanState.isNewOrDirty());

    selected = c.getSelected();
    assertNotNull(selected);

    String updateStmt = "update customer set smallnote = 'testTrans2' where id = :id";
    int rows = DB.createUpdate(Customer.class, updateStmt)
      .setParameter("id", custId).execute();
    assertEquals(1, rows);
    assertEquals("testTrans2", findNote(custId).get());

    rows = DB.createUpdate(Customer.class, "update customer set smallnote = ? where id = ?")
      .setNull(1, Types.VARCHAR)
      .setParameter(2, custId).execute();
    assertEquals(1, rows);
    assertThat(findNote(custId)).isEmpty();

    rows = DB.createUpdate(Customer.class, "update customer set smallnote = ? where id = ?")
      .setParameter(1, "Foo")
      .setParameter(2, custId).execute();
    assertEquals(1, rows);
    assertEquals("Foo", findNote(custId).get());

    rows = DB.createUpdate(Customer.class, "update customer set smallnote = :name where id = :id")
      .setNullParameter("name", Types.VARCHAR)
      .setParameter("id", custId).execute();
    assertEquals(1, rows);
    assertThat(findNote(custId)).isEmpty();

    // cleanup
    DB.delete(Customer.class, custId);
  }

  private Optional<String> findNote(Integer custId) {
    return DB.sqlQuery("select smallnote from o_customer where id = ?")
        .setParameter(custId)
        .mapToScalar(String.class).findOneOrEmpty();
  }

}
