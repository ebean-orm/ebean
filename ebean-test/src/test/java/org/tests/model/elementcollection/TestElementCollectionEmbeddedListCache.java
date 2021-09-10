package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedListCache extends BaseTestCase {

  @Test
  public void test() {

    EcblPerson person = new EcblPerson("CacheL");
    person.getPhoneNumbers().add(new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().add(new EcPhone("64","021","4321"));
    DB.save(person);

    EcblPerson one = DB.find(EcblPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    LoggedSql.start();

    one.getPhoneNumbers().size();

    List<String> sql = LoggedSql.collect();
    assertThat(sql).isEmpty();

    EcblPerson two = DB.find(EcblPerson.class)
      .setId(person.getId())
      .findOne();

    two.getPhoneNumbers().size();
    assertThat(two.getPhoneNumbers().toString()).contains("64-021-1234", "64-021-4321");

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit

    two.getPhoneNumbers().add(new EcPhone("61", "07", "11"));
    two.getPhoneNumbers().remove(1);

    DB.save(two);

    sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5); // update of collection only
      assertSql(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(2)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertSqlBind(sql, 3, 4);
    } else {
      assertThat(sql).hasSize(3); // update of collection only
      assertSql(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSql(sql.get(1)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertSql(sql.get(2)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
    }

    EcblPerson three = DB.find(EcblPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getPhoneNumbers().toString()).contains("61-07-11", "64-021-1234");
    assertThat(three.getPhoneNumbers()).hasSize(2);

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit


    three.setName("mod-3");
    three.getPhoneNumbers().remove(0);

    DB.save(three);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(5);

    EcblPerson four = DB.find(EcblPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(four.getPhoneNumbers().toString()).contains("61-07-11");
    assertThat(four.getPhoneNumbers()).hasSize(1);


    DB.delete(four);
    sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);


    LoggedSql.stop();
  }
}
