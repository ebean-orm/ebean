package com.avaje.tests.delete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.EBasicVer;
import com.avaje.tests.model.converstation.Group;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestDeleteWithoutOptimisticLocking extends BaseTestCase {

  @Test
  public void testSimpleBeanDelete_missingBean_returnsFalse() {

    // delete by by without version loaded ... should not throw OptimisticLockException
    Contact ref = Ebean.getReference(Contact.class, 999999);
    assertThat(Ebean.delete(ref)).isFalse();

    assertThat(Ebean.delete(Contact.class, 999999)).isEqualTo(0);

    // same as above but using Model.delete()
    Group modelRef = Ebean.getReference(Group.class, 999999);
    assertThat(modelRef.delete()).isFalse();
  }

  @Test
  public void testSimpleBeanDelete_existingBean_returnsTrue() {

    EBasicVer basic = new EBasicVer();
    basic.setName("DelTest");
    Ebean.save(basic);

    EBasicVer basicRef = Ebean.getReference(EBasicVer.class, basic.getId());
    assertThat(Ebean.delete(basicRef)).isTrue();
  }


  @Test
  public void testSimpleBeanDelete_existingBeanWithJdbcBatch_returnsTrue() {

    EBasicVer basic = new EBasicVer();
    basic.setName("DelTestBatch");
    Ebean.save(basic);

    EbeanServer server = Ebean.getDefaultServer();
    Transaction transaction = server.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.ALL);

      // returns true even though the delete has not occurred yet
      assertThat(server.delete(Ebean.getReference(EBasicVer.class, basic.getId()), transaction)).isTrue();

      // returns true even though the bean does not exist
      assertThat(server.delete(Ebean.getReference(EBasicVer.class, 999999), transaction)).isTrue();

      transaction.commit();

      assertThat(Ebean.find(EBasicVer.class, basic.getId())).isNull();

    } finally {
      transaction.end();
    }
  }

}
