package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedMapCache extends BaseTestCase {

  @Test
  public void test() {

    EcbmPerson person = new EcbmPerson("Cache1");
    person.getPhoneNumbers().put("home", new EcPhone("64", "021", "1234"));
    person.getPhoneNumbers().put("work", new EcPhone("64", "021", "4321"));
    DB.save(person);


    EcbmPerson one = DB.find(EcbmPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    LoggedSql.start();

    one.getPhoneNumbers().size();

    List<String> sql = LoggedSql.collect();
    assertThat(sql).isEmpty();

    EcbmPerson two = DB.find(EcbmPerson.class)
      .setId(person.getId())
      .findOne();

    two.getPhoneNumbers().size();
    assertThat(two.getPhoneNumbers().toString()).contains("64-021-1234", "64-021-4321");

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit

    two.getPhoneNumbers().put("mob", new EcPhone("61", "07", "11"));
    two.getPhoneNumbers().remove("home");

    DB.save(two);

    sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5); // update of collection only
      assertSql(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(2)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertSqlBind(sql, 3, 4);
    } else {
      assertThat(sql).hasSize(4); // update of collection only
      assertSql(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(2)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
    }

    EcbmPerson three = DB.find(EcbmPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getPhoneNumbers().toString()).contains("61-07-11", "64-021-4321");
    assertThat(three.getPhoneNumbers()).hasSize(2);

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty(); // cache hit


    three.setName("mod-3");
    three.getPhoneNumbers().remove("work");

    DB.save(three);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(5);

    EcbmPerson four = DB.find(EcbmPerson.class)
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
