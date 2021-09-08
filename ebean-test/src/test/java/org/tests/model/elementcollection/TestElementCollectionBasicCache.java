package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.PersistBatch;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicCache extends BaseTestCase {

  @Test
  public void test() {

    EcPerson person = new EcPerson("Cache1");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    DB.save(person);

    EcPerson one = DB.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    one.getPhoneNumbers().size();

    LoggedSql.start();

    EcPerson two = DB.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    two.setName("CacheMod");
    two.getPhoneNumbers().add("027 234234");

    List<String> sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit containing phone numbers

    DB.save(two);

    sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(7);
      assertSqlBind(sql, 4, 6);
    } else {
      assertThat(sql).hasSize(5);
    }

    DB.save(two);

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // no change

    EcPerson three = DB.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getName()).isEqualTo("CacheMod");
    assertThat(three.getPhoneNumbers()).contains("021 1234", "021 4321", "027 234234");

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit

    three.getPhoneNumbers().add("09 6534");
    DB.save(three);

    sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(7); // cache hit
    } else {
      assertThat(sql).hasSize(5); // cache hit
    }

    EcPerson four = DB.find(EcPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    assertThat(four.getName()).isEqualTo("CacheMod");
    assertThat(four.getPhoneNumbers()).contains("021 1234", "021 4321", "027 234234", "09 6534");

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit

    DB.delete(four);

    LoggedSql.stop();
  }

  @Override
  public boolean isPersistBatchOnCascade() {
    return ((SpiEbeanServer) DB.getDefault()).databasePlatform().getPersistBatchOnCascade() != PersistBatch.NONE;
  }
}
