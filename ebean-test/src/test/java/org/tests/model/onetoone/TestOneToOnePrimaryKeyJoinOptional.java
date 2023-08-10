package org.tests.model.onetoone;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.plugin.Property;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class TestOneToOnePrimaryKeyJoinOptional extends BaseTestCase {


  private OtoUPrime insert(String desc) {
    OtoUPrime prime = new OtoUPrime("u" + desc);
    OtoUPrimeExtra extra = new OtoUPrimeExtra("v" + desc);
    prime.setExtra(extra);
    DB.save(prime);
    return prime;
  }

  @BeforeEach
  void prepare() {
    OtoUPrime p1Single = new OtoUPrime("Prime without optional");
    p1Single.setExtra(new OtoUPrimeExtra("Non optional prime required"));
    DB.save(p1Single);

    OtoUPrimeExtra p2 = new OtoUPrimeExtra("SinglePrimeExtra");
    try {
      DB.save(p2);
      fail("PrimExtra cannot exist without Prime");
    } catch (PersistenceException pe) {

    }

    String desc = "" + System.currentTimeMillis();
    OtoUPrime p1 = new OtoUPrime("u" + desc);
    p1.setExtra(new OtoUPrimeExtra("u" + desc));
    p1.setOptionalExtra(new OtoUPrimeOptionalExtra("This one has also an optional"));
    DB.save(p1);
  }

  @AfterEach
  void cleanup() {
    DB.find(OtoUPrime.class).delete();
    assertThat(DB.find(OtoUPrimeExtra.class).findList()).isEmpty();
    assertThat(DB.find(OtoUPrimeOptionalExtra.class).findList()).isEmpty();
  }

  public void doTest1(boolean extraFetch, boolean optionalFetch) {

    // Query for "fetch" case - extra bean joined by left join

    Query<OtoUPrime> query1 = DB.find(OtoUPrime.class);
    if (extraFetch) {
      query1.fetch("extra");
    }
    if (optionalFetch) {
      query1.fetch("optionalExtra");
    }
    List<OtoUPrime> primes = query1.findList();
    if (extraFetch && optionalFetch) {
      assertThat(query1.getGeneratedSql()).isEqualTo("select t0.pid, t0.name, t0.version, " +
        "t1.eid, t1.extra, t1.version, " +
        "t2.eid, t2.extra, t2.version, t2.eid " +
        "from oto_uprime t0 " +
        "left join oto_uprime_extra t1 on t1.eid = t0.pid " +  // left join on non-optional, because DbForeignKey(noConstraint=true) is set
        "left join oto_uprime_optional_extra t2 on t2.eid = t0.pid"); // left join on optional
    } else if (extraFetch) {
      assertThat(query1.getGeneratedSql()).isEqualTo("select t0.pid, t0.name, t0.version, t1.eid, t1.extra, t1.version from oto_uprime t0 left join oto_uprime_extra t1 on t1.eid = t0.pid");
    } else if (optionalFetch) {
      assertThat(query1.getGeneratedSql()).isEqualTo("select t0.pid, t0.name, t0.pid, t0.version, t1.eid, t1.extra, t1.version, t1.eid from oto_uprime t0 left join oto_uprime_optional_extra t1 on t1.eid = t0.pid");

    } else {
      assertThat(query1.getGeneratedSql()).isEqualTo("select t0.pid, t0.name, t0.pid, t0.version from oto_uprime t0");
    }

    List<Long> versions = new ArrayList<>();
    for (OtoUPrime prime : primes) {
      if (prime.getOptionalExtra() != null) {
        versions.add(prime.getOptionalExtra().getVersion());
      }
    }
    assertThat(primes).hasSize(2);
    assertThat(versions).containsExactly(1L);
  }

  public void doTest2(boolean withFetch) {

    Query<OtoUPrimeOptionalExtra> query2 = DB.find(OtoUPrimeOptionalExtra.class);
    if (withFetch) {
      query2.fetch("prime");
    }
    List<OtoUPrimeOptionalExtra> extraPrimes = query2.findList();
    if (withFetch) {
      assertThat(query2.getGeneratedSql()).isEqualTo("select t0.eid, t0.extra, t0.version, t1.pid, t1.name, t1.pid, t1.version from oto_uprime_optional_extra t0 join oto_uprime t1 on t1.pid = t0.eid");
    } else {
      assertThat(query2.getGeneratedSql()).isEqualTo("select t0.eid, t0.extra, t0.version, t0.eid from oto_uprime_optional_extra t0");
    }
    List<Long> versions = new ArrayList<>();
    for (OtoUPrimeOptionalExtra extraPrime : extraPrimes) {
      versions.add(extraPrime.getPrime().getVersion());
    }
    assertThat(extraPrimes).hasSize(1);
    assertThat(versions).containsExactly(1L);
  }

  @Test
  void testWithExtraFetch1() {
    doTest1(true, false);
  }

  @Test
  void testWithOptionalFetch1() {
    doTest1(false, true);
  }

  @Test
  void testWithBothFetch1() {
    doTest1(true, true);
  }

  @Test
  void testWithoutFetch1() {
    doTest1(false, false);
  }

  @Test
  void testWithFetch2() {
    doTest2(true);
  }

  @Test
  void testWithoutFetch2() {
    doTest2(false);
  }

  @Test
  public void insertUpdateDelete() {

    String desc = "" + System.currentTimeMillis();
    OtoUPrime p1 = insert(desc);

    assertThat(p1.getExtra().getEid()).isEqualTo(p1.getPid()).as("Same id values");


    Query<OtoUPrime> query = DB.find(OtoUPrime.class).setId(p1.getPid());

    OtoUPrime found = query.findOne();

    assertThat(found).isNotNull();
    assertThat(sqlOf(query, 4)).contains("select t0.pid, t0.name, t0.pid, t0.version from oto_uprime t0 where t0.pid = ?")
      .as("we don't join to oto_uprime_extra");

    assertThat(found.getName()).isEqualTo("u" + desc);

    Query<OtoUPrime> queryWithFetch = DB.find(OtoUPrime.class)
      .setId(p1.getPid())
      .fetch("extra");

    OtoUPrime oneWith = queryWithFetch.findOne();

    assertThat(oneWith).isNotNull();
    assertThat(sqlOf(queryWithFetch, 6))
      .contains("select t0.pid, t0.name, t0.version, t1.eid, t1.extra, t1.version from oto_uprime t0 left join oto_uprime_extra t1 on t1.eid = t0.pid where t0.pid = ?")
      .as("we join to oto_prime_extra");


    assertThat(oneWith.getExtra().getExtra()).isEqualTo("v" + desc);

    thenUpdate(oneWith);
    thenDelete(found);
  }

  private void thenUpdate(OtoUPrime oneWith) {

    OtoUPrimeExtra extra = oneWith.getExtra();
    extra.setExtra("modified");

    DB.save(oneWith);

    extra.setExtra("mod2");
    oneWith.setName("mod2");

    DB.save(oneWith);
  }

  private void thenDelete(OtoUPrime found) {


    OtoUPrime bean = DB.find(OtoUPrime.class, found.getPid());

    LoggedSql.start();
    DB.delete(bean);

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from oto_uprime_extra where");
    assertSql(sql.get(1)).contains("delete from oto_uprime_optional_extra where");
    assertSql(sql.get(2)).contains("delete from oto_uprime where");
  }

  @Test
  void testDdl() {
    Collection<? extends Property> props = DB.getDefault().pluginApi().beanType(OtoUPrime.class).allProperties();

    for (Property prop : props) {
      System.out.println(prop);
    }
  }

  @Test
  void testContractViolation1() {

    OtoUPrime p1 = new OtoUPrime("Prime having no extra");
    // extra is "optional=false" - and this is a violating of the contract
    DB.save(p1);

    Query<OtoUPrime> query = DB.find(OtoUPrime.class).setId(p1.pid);

    OtoUPrime found1 = query.findOne();
    assertThat(query.getGeneratedSql()).doesNotContain("join");

    assertThat(found1.getExtra()).isNotNull();
    assertThatThrownBy(() -> found1.getExtra().getVersion()).isInstanceOf(EntityNotFoundException.class);

    query.fetch("extra");
    OtoUPrime found2 = query.findOne();
    // Note: We use "left join" here, because 'DbForeignKey(noConstraint=true)' is set oh the property
    // if this annotation is not preset, an inner join would be used and 'found2' would be 'null' then
    assertThat(query.getGeneratedSql()).contains("from oto_uprime t0 left join oto_uprime_extra");
    assertThat(found2.getExtra()).isNull();

  }

  @Test
  void testContractViolation2() {

    OtoUPrimeExtraWithConstraint p1Const = new OtoUPrimeExtraWithConstraint("test");
    try {
      // a foreign key prevents from saving
      DB.save(p1Const);
      fail("PrimExtra cannot exist without Prime");
    } catch (PersistenceException pe) {
      // OK
    }

    OtoUPrimeWithConstraint p1 = new OtoUPrimeWithConstraint("Prime having no extra");
    // extra is "optional=false" - and this is a violating of the contract
    // Note there is no real foreign key in the database, that would prevent saving this entity
    DB.save(p1);

    Query<OtoUPrimeWithConstraint> query = DB.find(OtoUPrimeWithConstraint.class).setId(p1.pid);

    OtoUPrimeWithConstraint found1 = query.findOne();
    assertThat(query.getGeneratedSql()).doesNotContain("join");

    assertThat(found1.getExtra()).isNotNull();
    assertThatThrownBy(() -> found1.getExtra().getVersion()).isInstanceOf(EntityNotFoundException.class);

    query.fetch("extra");
    OtoUPrimeWithConstraint found2 = query.findOne();
    // Note: We use "left join" here, because 'DbForeignKey(noConstraint=true)' is set oh the property
    // if this annotation is not preset, an inner join would be used and 'found2' would be 'null' then
    assertThat(query.getGeneratedSql()).contains("from oto_uprime_with_constraint t0 join oto_uprime_extra_with_constraint");
    assertThat(found2).isNull();

  }
}
