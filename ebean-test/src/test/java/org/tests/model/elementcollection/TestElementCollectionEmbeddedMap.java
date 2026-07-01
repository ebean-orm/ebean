package org.tests.model.elementcollection;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedMap extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();

    EcbmPerson person = new EcbmPerson("Fiona64021");
    person.getPhoneNumbers().put("home", new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().put("work", new EcPhone("64","021","4321"));
    DB.save(person);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("insert into ecbm_person");
      assertSql(sql.get(1)).contains("insert into ecbm_person_phone_numbers");
      assertSqlBind(sql, 2, 3);
      assertThat(sql.get(4)).contains(" -- executeBatch");
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ecbm_person");
      assertSql(sql.get(1)).contains("insert into ecbm_person_phone_numbers");
      assertSql(sql.get(2)).contains("insert into ecbm_person_phone_numbers");
    }

    EcbmPerson person1 = new EcbmPerson("Fiona6409");
    person1.getPhoneNumbers().put("home",new EcPhone("64","09","1234"));
    person1.getPhoneNumbers().put("mob",new EcPhone("61","09","4321"));

    DB.save(person1);

    LoggedSql.collect();

    List<EcbmPerson> found =
      DB.find(EcbmPerson.class).where()
        .startsWith("name", "Fiona640")
        .orderBy().asc("id")
        .findList();

    Map<String,EcPhone> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Map<String,EcPhone> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0.toString()).contains("64-021-1234", "64-021-4321");
    assertThat(phoneNumbers1.toString()).contains("64-09-1234", "61-09-4321");

    List<EcbmPerson> found2 =
      DB.find(EcbmPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona640")
        .orderBy().asc("id")
        .findList();

    assertThat(found2).hasSize(2);
    EcbmPerson foundFirst = found2.get(0);

    LoggedSql.collect();

    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSql.stop();
  }

  private void updateBasic(EcbmPerson bean) {

    bean.setName("Fiona64-mod-0");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update ecbm_person");

    updateBoth(bean);
  }

  private void updateBoth(EcbmPerson bean) {

    bean.setName("Fiona64-mod-both");
    bean.getPhoneNumbers().put("more", new EcPhone("01", "234", "123"));
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("update ecbm_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertThat(sql.get(4)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum)");
      assertSqlBind(sql, 5, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("update ecbm_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertSql(sql.get(2)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum)");
      assertThat(sql.get(3)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum)");
      assertThat(sql.get(4)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum)");
    }

    updateNothing(bean);
  }

  private void updateNothing(EcbmPerson bean) {

    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcbmPerson bean) {

    bean.getPhoneNumbers().put("other", new EcPhone("01", "12", "4321"));
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(3)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertSqlBind(sql, 4, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
      assertSql(sql.get(1)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertSql(sql.get(2)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
      assertThat(sql.get(4)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,phnum) values (?,?,?,?,?)");
    }

    delete(bean);
  }

  private void delete(EcbmPerson bean) {

    DB.delete(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id = ?");
    assertSql(sql.get(1)).contains("delete from ecbm_person where id=? and version=?");
  }

  private void jsonToFrom(EcbmPerson foundFirst) {
    String asJson = DB.json().toJson(foundFirst);

    EcbmPerson fromJson = DB.json().toBean(EcbmPerson.class, asJson);

    String phoneString = fromJson.getPhoneNumbers().toString();
    assertThat(phoneString).contains("64-021-1234");
    assertThat(phoneString).contains("64-021-4321");
  }

  @Test
  public void json() {

    EcbmPerson person = new EcbmPerson("Fiona64021");
    person.getPhoneNumbers().put("home", new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().put("work", new EcPhone("64","021","4321"));

    final String asJson = DB.json().toJson(person);

    assertThat(asJson).isEqualTo("{\"name\":\"Fiona64021\",\"phoneNumbers\":{\"home\":{\"countryCode\":\"64\",\"area\":\"021\",\"number\":\"1234\"},\"work\":{\"countryCode\":\"64\",\"area\":\"021\",\"number\":\"4321\"}}}");

    final EcbmPerson fromJson = DB.json().toBean(EcbmPerson.class, asJson);
    assertThat(fromJson.getName()).isEqualTo("Fiona64021");
    assertThat(fromJson.getPhoneNumbers()).hasSize(2);
    assertThat(fromJson.getPhoneNumbers().toString()).isEqualTo("{home=64-021-1234, work=64-021-4321}");
  }
}
