package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOnePrimaryKeyJoinOptional extends BaseTestCase {


  private OtoUPrime insert(String desc) {
    OtoUPrime prime = new OtoUPrime("u" + desc);
    OtoUPrimeExtra extra = new OtoUPrimeExtra("v" + desc);
    prime.setExtra(extra);
    Ebean.save(prime);
    return prime;
  }

  @Test
  public void insertWithoutExtra() {

    String desc = "" + System.currentTimeMillis();
    OtoUPrime p1 = new OtoUPrime("u" + desc);
    Ebean.save(p1);

    Query<OtoUPrime> query = Ebean.find(OtoUPrime.class)
      .setId(p1.getPid())
      .fetch("extra", "eid");

    OtoUPrime found = query.findOne();

    if (found.getExtra() != null) {
      found.getExtra().getExtra(); // fails here, because getExtra should be null
    }
    assertThat(found.getExtra()).isNull();
  }

  @Test
  public void insertUpdateDelete() {

    String desc = "" + System.currentTimeMillis();
    OtoUPrime p1 = insert(desc);

    assertThat(p1.getExtra().getEid()).isEqualTo(p1.getPid()).as("Same id values");


    Query<OtoUPrime> query = Ebean.find(OtoUPrime.class).setId(p1.getPid());

    OtoUPrime found = query.findOne();

    assertThat(found).isNotNull();
    assertThat(sqlOf(query, 4)).contains("select t0.pid, t0.name, t0.version, t0.pid from oto_uprime t0 where t0.pid = ?")
      .as("we don't join to oto_uprime_extra");

    assertThat(found.getName()).isEqualTo("u" + desc);

    Query<OtoUPrime> queryWithFetch = Ebean.find(OtoUPrime.class)
      .setId(p1.getPid())
      .fetch("extra");

    OtoUPrime oneWith = queryWithFetch.findOne();

    assertThat(oneWith).isNotNull();
    assertThat(sqlOf(queryWithFetch, 6)).contains("select t0.pid, t0.name, t0.version, t1.eid, t1.extra, t1.version from oto_uprime t0 left join oto_uprime_extra t1 on t1.eid = t0.pid  where t0.pid = ?")
      .as("we join to oto_prime_extra");


    assertThat(oneWith.getExtra().getExtra()).isEqualTo("v" + desc);

    thenUpdate(oneWith);
    thenDelete(found);
  }

  private void thenUpdate(OtoUPrime oneWith) {

    OtoUPrimeExtra extra = oneWith.getExtra();
    extra.setExtra("modified");

    Ebean.save(oneWith);

    extra.setExtra("mod2");
    oneWith.setName("mod2");

    Ebean.save(oneWith);
  }

  private void thenDelete(OtoUPrime found) {


    OtoUPrime bean = Ebean.find(OtoUPrime.class, found.getPid());

    LoggedSqlCollector.start();
    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from oto_uprime_extra where");
    assertThat(sql.get(1)).contains("delete from oto_uprime where");

  }
}
