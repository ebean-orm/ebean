package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.EBasicVer;
import org.tests.model.converstation.Group;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteWithoutOptimisticLocking extends BaseTestCase {

  @Test
  public void testSimpleBeanDelete_missingBean_returnsFalse() {

    // delete by by without version loaded ... should not throw OptimisticLockException
    Contact ref = DB.getReference(Contact.class, 999999);
    assertThat(DB.delete(ref)).isFalse();

    assertThat(DB.delete(Contact.class, 999999)).isEqualTo(0);

    // same as above but using Model.delete()
    Group modelRef = DB.getReference(Group.class, 999999);
    assertThat(modelRef.delete()).isFalse();
  }

  @Test
  public void testSimpleBeanDelete_existingBean_returnsTrue() {

    EBasicVer basic = new EBasicVer("DelTest");
    DB.save(basic);

    EBasicVer basicRef = DB.getReference(EBasicVer.class, basic.getId());
    assertThat(DB.delete(basicRef)).isTrue();
  }


  @Test
  public void testSimpleBeanDelete_existingBeanWithJdbcBatch_returnsTrue() {

    EBasicVer basic = new EBasicVer("DelTestBatch");
    DB.save(basic);

    Database server = DB.getDefault();
    Transaction transaction = server.beginTransaction();
    try {
      transaction.setBatchMode(true);

      // returns true even though the delete has not occurred yet
      assertThat(server.delete(DB.getReference(EBasicVer.class, basic.getId()), transaction)).isTrue();

      // returns true even though the bean does not exist
      assertThat(server.delete(DB.getReference(EBasicVer.class, 999999), transaction)).isTrue();

      transaction.commit();

      assertThat(DB.find(EBasicVer.class, basic.getId())).isNull();

    } finally {
      transaction.end();
    }
  }

}
