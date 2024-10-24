package org.tests.json;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.ValuePair;
import io.ebean.DatabaseBuilder;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.MutationDetection;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonList;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestJsonSourceDefault {

  @Test
  @ForPlatform(Platform.H2)
  @Disabled
  void testDirtyValues_diffSource() {
    DatabaseConfig config = new DatabaseConfig();
    config.getDataSourceConfig()
      .username("sa")
      .password("")
      .url("jdbc:h2:mem:testJsonSourceDirtyValues");

    config.setName("jsonSource");
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.addClass(EBasicJsonList.class);
    config.setJsonMutationDetection(MutationDetection.SOURCE);
    Database db = DatabaseFactory.create(config);
    try {
      assertThat(db).isNotNull();

      EBasicJsonList bean = new EBasicJsonList();
      bean.getTags().add("aa");
      bean.getTags().add("bb");

      db.save(bean);
      bean = db.find(EBasicJsonList.class, bean.getId());

      bean.getTags().add("cc");
      final Map<String, ValuePair> dirtyValues = db.beanState(bean).dirtyValues();
      assertThat(dirtyValues).containsOnlyKeys("tags");

      final ValuePair diff = dirtyValues.get("tags");
      assertThat(diff.getOldValue()).isInstanceOf(List.class).asList().containsExactly("aa", "bb");
      assertThat(diff.getNewValue()).isInstanceOf(List.class).asList().containsExactly("aa", "bb", "cc");
    } finally {
      if (db != null) {
        db.shutdown();
      }
    }
  }
}
