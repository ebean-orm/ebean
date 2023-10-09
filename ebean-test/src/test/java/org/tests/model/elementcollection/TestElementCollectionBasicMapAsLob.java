package org.tests.model.elementcollection;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicMapAsLob extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();

    EcmcPerson person = new EcmcPerson("Lob021");
    person.getPhoneNumbers().put("home", "021 1234");
    person.getPhoneNumbers().put("work", "021 4321");
    DB.save(person);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("insert into ecmc_person");
      assertSql(sql.get(1)).contains("insert into ecmc_person_phone");
      assertSqlBind(sql, 2, 3);
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ecmc_person");
      assertSql(sql.get(1)).contains("insert into ecmc_person_phone");
      assertSql(sql.get(2)).contains("insert into ecmc_person_phone");
    }

    LoggedSql.stop();
  }

}
