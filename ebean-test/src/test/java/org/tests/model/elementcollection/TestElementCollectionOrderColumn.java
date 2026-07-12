package org.tests.model.elementcollection;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for issue #2393 - {@code @OrderColumn} on an {@code @ElementCollection}.
 */
public class TestElementCollectionOrderColumn extends BaseTestCase {

  @Test
  public void insert_populatesOrderColumn() {
    LoggedSql.start();

    EcolPerson person = new EcolPerson("OrderCol1");
    person.getPhoneNumbers().add(new EcPhone("64", "021", "1111"));
    person.getPhoneNumbers().add(new EcPhone("64", "021", "2222"));
    person.getPhoneNumbers().add(new EcPhone("64", "021", "3333"));
    DB.save(person);

    List<String> sql = LoggedSql.stop();

    // find the insert statements for the collection table and check ordinal column/values
    List<String> inserts = sql.stream()
      .filter(s -> s.contains("insert into ecol_person_phone_numbers"))
      .collect(Collectors.toList());

    assertThat(inserts).isNotEmpty();
    assertThat(inserts.get(0)).contains("ordinal");

    // reload and confirm ordering is maintained
    EcolPerson found = DB.find(EcolPerson.class, person.getId());
    List<EcPhone> phones = found.getPhoneNumbers();
    assertThat(phones).hasSize(3);
    assertThat(phones.get(0).getNumber()).isEqualTo("1111");
    assertThat(phones.get(1).getNumber()).isEqualTo("2222");
    assertThat(phones.get(2).getNumber()).isEqualTo("3333");

    DB.delete(person);
  }

  @Test
  public void fetch_hasOrderByOnOrdinal() {
    EcolPerson person = new EcolPerson("OrderCol2");
    person.getPhoneNumbers().add(new EcPhone("64", "021", "1111"));
    person.getPhoneNumbers().add(new EcPhone("64", "021", "2222"));
    DB.save(person);

    LoggedSql.start();

    EcolPerson found = DB.find(EcolPerson.class)
      .fetch("phoneNumbers")
      .where().idEq(person.getId())
      .findOne();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).isNotEmpty();
    String trimmed = trimSql(sql.get(0));
    assertThat(trimmed).contains("order by").contains("ordinal");

    assertThat(found.getPhoneNumbers()).hasSize(2);

    DB.delete(person);
  }
}
