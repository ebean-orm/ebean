package org.tests.model.onetoone;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOnePrimaryKeyJoinBidi extends BaseTestCase {

  private OtoUBPrime insert(String desc) {
    OtoUBPrime prime = new OtoUBPrime("u" + desc);
    OtoUBPrimeExtra extra = new OtoUBPrimeExtra("v" + desc);
    prime.setExtra(extra);
    DB.save(prime);
    return prime;
  }

  @Test
  public void insertUpdateDelete() {

    String desc = "" + System.currentTimeMillis();
    OtoUBPrime p1 = insert(desc);

    assertThat(p1.getExtra().getEid()).isEqualTo(p1.getPid()).as("Same id values");

    Query<OtoUBPrime> query = DB.find(OtoUBPrime.class).setId(p1.getPid());

    OtoUBPrime found = query.findOne();

    assertThat(found).isNotNull();
    assertThat(sqlOf(query, 10)).contains("select t0.pid, t0.name, t0.version, t0.pid from oto_ubprime t0 where t0.pid = ?")
      .as("we don't join to oto_ubprime_extra");

    assertThat(found.getName()).isEqualTo("u" + desc);

    Query<OtoUBPrime> queryWithFetch = DB.find(OtoUBPrime.class)
      .setId(p1.getPid())
      .fetch("extra");

    OtoUBPrime oneWith = queryWithFetch.findOne();

    assertThat(oneWith).isNotNull();
    assertThat(sqlOf(queryWithFetch, 10)).contains("select t0.pid, t0.name, t0.version, t1.eid, t1.extra, t1.version, t1.eid from oto_ubprime t0 join oto_ubprime_extra t1 on t1.eid = t0.pid where t0.pid = ?")
      .as("we join to oto_prime_extra");

    assertThat(oneWith.getExtra().getExtra()).isEqualTo("v" + desc);

    thenUpdate(oneWith);
    thenDelete(found);
  }

  private void thenUpdate(OtoUBPrime oneWith) {

    OtoUBPrimeExtra extra = oneWith.getExtra();
    extra.setExtra("modified");

    DB.save(oneWith);

    extra.setExtra("mod2");
    oneWith.setName("mod2");

    DB.save(oneWith);
  }

  private void thenDelete(OtoUBPrime found) {

    OtoUBPrime bean = DB.find(OtoUBPrime.class, found.getPid());

    LoggedSql.start();
    DB.delete(bean);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from oto_ubprime_extra where");
    assertSql(sql.get(1)).contains("delete from oto_ubprime where");
  }
}
