package org.tests.defaultvalues;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDefaults extends BaseTestCase {

  @Test
  public void testInsertDefaultValues() {
    final DefaultsModel main = new DefaultsModel();

    for (int i = 0; i < 5; i++) {
      final ReferencedDefaultsModel ref = new ReferencedDefaultsModel();
      ref.setName("r" + i);
      main.getRelatedModels().add(ref);
    }

    LoggedSql.start();
    DB.save(main);
    final List<String> current = LoggedSql.collect();

    assertThat(current).isNotEmpty();
    if (isMySql() || isMariaDB() || isOracle()) {
      assertThat(current.get(0)).contains("insert into defaults_model_draft values (default);");
    } else if (isDb2()) {
      assertThat(current.get(0)).contains("insert into defaults_model_draft (id) values (default)");
    } else if (isSqlServer()) {
      assertThat(current.get(0)).contains("insert into defaults_model_draft (id) values (?)");
    } else {
      assertThat(current.get(0)).contains("insert into defaults_model_draft default values;");
    }
  }

}
