package org.tests.defaultvalues;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

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

    LoggedSqlCollector.start();
    Ebean.save(main);
    final List<String> current = LoggedSqlCollector.current();

    assertThat(current).isNotEmpty();
    if (isMySql() || isMariaDB()) {
      assertThat(current.get(0)).contains("insert into defaults_model_draft values (default);");
    } else if (isSqlServer()) {
      assertThat(current.get(0)).contains("insert into defaults_model_draft (id) values (?)");
    } else {
      assertThat(current.get(0)).contains("insert into defaults_model_draft default values;");
    }
  }

}
