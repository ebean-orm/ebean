package org.tests.model.elementcollection;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedList extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();

    EcblPerson person = new EcblPerson("Fiona64021");
    person.getPhoneNumbers().add(new EcPhone("64", "021", "1234"));
    person.getPhoneNumbers().add(new EcPhone("64", "021", "4321"));
    DB.save(person);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("insert into ecbl_person");
      assertSql(sql.get(1)).contains("insert into ecbl_person_phone_numbers");
      assertSqlBind(sql, 2, 3);
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ecbl_person");
      assertSql(sql.get(1)).contains("insert into ecbl_person_phone_numbers");
      assertSql(sql.get(2)).contains("insert into ecbl_person_phone_numbers");
    }

    EcblPerson person1 = new EcblPerson("Fiona6409");
    person1.getPhoneNumbers().add(new EcPhone("61", "09", "1234"));
    person1.getPhoneNumbers().add(new EcPhone("64", "09", "4321"));
    DB.save(person1);

    LoggedSql.collect();

    List<EcblPerson> found =
      DB.find(EcblPerson.class).where()
        .startsWith("name", "Fiona640")
        .orderBy().asc("id")
        .findList();

    List<EcPhone> phoneNumbers0 = found.get(0).getPhoneNumbers();
    List<EcPhone> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0.toString()).contains("64-021-1234", "64-021-4321");
    assertThat(phoneNumbers1.toString()).contains("61-09-1234", "64-09-4321");

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version from ecbl_person");
    assertSql(sql.get(1)).contains("select t0.person_id, t0.country_code, t0.area, t0.phnum from ecbl_person_phone_numbers");

    List<EcblPerson> found2 =
      DB.find(EcblPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona640")
        .orderBy().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    String trimmedSql = trimSql(sql.get(0));
    assertThat(trimmedSql).contains("select t0.id, t0.name, t0.version, t1.country_code, t1.area, t1.phnum from ecbl_person t0 left join ecbl_person_phone_numbers t1");


    EcblPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSql.stop();
  }

  private void updateBasic(EcblPerson bean) {

    bean.setName("Fiona64-mod-0");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update ecbl_person");

    updateBoth(bean);
  }

  private void updateBoth(EcblPerson bean) {

    bean.setName("Fiona64-mod-both");
    bean.getPhoneNumbers().add(new EcPhone("01", "234", "123"));
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("update ecbl_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(2));
      assertThat(sql.get(4)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertSqlBind(sql, 5, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("update ecbl_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSql(sql.get(2)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertThat(sql.get(4)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
    }

    updateNothing(bean);
  }

  private void updateNothing(EcblPerson bean) {

    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcblPerson bean) {

    bean.getPhoneNumbers().add(new EcPhone("01", "12", "4321"));
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(3)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertSqlBind(sql, 4, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
      assertSql(sql.get(1)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertSql(sql.get(2)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
      assertThat(sql.get(4)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,phnum) values (?,?,?,?)");
    }

    delete(bean);
  }

  private void delete(EcblPerson bean) {

    DB.delete(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id = ?");
    assertSql(sql.get(1)).contains("delete from ecbl_person where id=? and version=?");
  }

  private void jsonToFrom(EcblPerson foundFirst) {

    String asJson = DB.json().toJson(foundFirst);

    EcblPerson fromJson = DB.json().toBean(EcblPerson.class, asJson);

    String phoneString = fromJson.getPhoneNumbers().toString();
    assertThat(phoneString).contains("64-021-1234");
    assertThat(phoneString).contains("64-021-4321");
  }

  @Test
  public void json() {

    EcblPerson person = new EcblPerson("Fiona64021");
    person.getPhoneNumbers().add(new EcPhone("64", "021", "1234"));
    person.getPhoneNumbers().add(new EcPhone("64", "021", "4321"));

    final String asJson = DB.json().toJson(person);

    assertThat(asJson).isEqualTo("{\"name\":\"Fiona64021\",\"phoneNumbers\":[{\"countryCode\":\"64\",\"area\":\"021\",\"number\":\"1234\"},{\"countryCode\":\"64\",\"area\":\"021\",\"number\":\"4321\"}]}");

    final EcblPerson fromJson = DB.json().toBean(EcblPerson.class, asJson);
    assertThat(fromJson.getName()).isEqualTo("Fiona64021");
    assertThat(fromJson.getPhoneNumbers()).hasSize(2);
    assertThat(fromJson.getPhoneNumbers().toString()).isEqualTo("[64-021-1234, 64-021-4321]");
  }
}
