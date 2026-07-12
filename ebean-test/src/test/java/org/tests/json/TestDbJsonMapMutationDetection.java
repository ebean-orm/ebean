package org.tests.json;

import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonMapMutation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@code Map<String,Object>} @DbJson properties honour mutationDetection
 * rather than always using ModifyAware based dirty checking - see issue #2840.
 */
public class TestDbJsonMapMutationDetection extends BaseTestCase {

  private static Map<String, Object> content(String value) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("field", List.of(value));
    return map;
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void noneMode_inPlaceMutation_notDetected() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setNoneMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    // mutate the map content directly (without calling the setter)
    found.getNoneMap().put("field", List.of("mutated"));

    LoggedSql.start();
    DB.save(found);
    // NONE means no attempt to detect mutation - so no update at all
    assertThat(LoggedSql.stop()).isEmpty();
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void noneMode_setEqualValue_noUpdate() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setNoneMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    // set a brand new (but equal content) map instance via the setter
    found.setNoneMap(content("a"));

    BeanState state = DB.beanState(found);
    assertThat(state.changedProps()).isEmpty();

    LoggedSql.start();
    DB.save(found);
    assertThat(LoggedSql.stop()).isEmpty();
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void noneMode_setDifferentValue_updates() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setNoneMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    found.setNoneMap(content("b"));

    LoggedSql.start();
    DB.save(found);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("none_map");
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void hashMode_inPlaceMutation_detected() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setHashMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    found.getHashMap().put("field", List.of("mutated"));

    LoggedSql.start();
    DB.save(found);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("hash_map");
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void hashMode_setEqualValue_noUpdate() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setHashMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    // brand new map instance with equal content - hash should match so no update
    found.setHashMap(content("a"));

    LoggedSql.start();
    DB.save(found);
    assertThat(LoggedSql.stop()).isEmpty();
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void sourceMode_setEqualValue_noUpdate() {
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setSourceMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    found.setSourceMap(content("a"));

    LoggedSql.start();
    DB.save(found);
    assertThat(LoggedSql.stop()).isEmpty();
  }

  @IgnorePlatform(Platform.MYSQL)
  @Test
  public void defaultMode_inPlaceMutation_stillDetectedViaModifyAware() {
    // unchanged legacy behaviour - DEFAULT mode keeps using ModifyAware wrapper dirty checking
    EBasicJsonMapMutation bean = new EBasicJsonMapMutation();
    bean.setDefaultMap(content("a"));
    DB.save(bean);

    EBasicJsonMapMutation found = DB.find(EBasicJsonMapMutation.class, bean.getId());
    found.getDefaultMap().put("field", List.of("mutated"));

    LoggedSql.start();
    DB.save(found);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("default_map");
  }
}
