package org.tests.lazyloadconf;

import io.ebean.DB;
import io.ebean.Query;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanCollectionLazyLoadingTest {

  @Test
  public void test() {

    AppConfig globalAppConfig = new AppConfig();
    globalAppConfig.setId(1);
    globalAppConfig.setItems(new ArrayList<>());
    AppConfigControl global = new AppConfigControl();
    global.setId(1);
    global.setName("global");
    global.setAppConfig(globalAppConfig);
    globalAppConfig.getItems().add(global);
    DB.save(globalAppConfig);

    AppConfig userAppConfig = new AppConfig();
    userAppConfig.setId(2);
    userAppConfig.setItems(new ArrayList<>());
    AppConfigControl user = new AppConfigControl();
    user.setId(2);
    user.setName("user");
    user.setAppConfig(userAppConfig);
    userAppConfig.getItems().add(user);
    DB.save(userAppConfig);

    AppConfig otherAppConfig = new AppConfig();
    otherAppConfig.setId(3);
    otherAppConfig.setItems(new ArrayList<>());
    DB.save(otherAppConfig);

    Relationship globalRe = new Relationship();
    globalRe.setId(1);
    globalRe.setAppConfig(globalAppConfig);
    DB.save(globalRe);

    Relationship userRe = new Relationship();
    userRe.setId(2);
    userRe.setAppConfig(userAppConfig);
    DB.save(userRe);

    Relationship otherRe = new Relationship();
    otherRe.setId(3);
    otherRe.setAppConfig(otherAppConfig);
    DB.save(otherRe);


    // Start business processing
    Query<Relationship> relationshipQuery = DB.find(Relationship.class);
    List<Relationship> relationshipList = relationshipQuery.where().idIn(1, 2, 3).findList();

    assertThat(relationshipList.size()).isEqualTo(3);

    Map<Integer, AppConfig> map = relationshipList.stream()
      .map(Relationship::getAppConfig)
      .map((ac) -> new AbstractMap.SimpleImmutableEntry<>(ac.getId(), ac))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    AppConfig g = map.get(1);
    AppConfig u = map.get(2);
    AppConfig o = map.get(3);
    if (!u.getItems().isEmpty()) {
      // a source of the problem came from invoking lazy loading here
      // with the setItems call which is unnecessary due to being a ToMany
      g.setItems(u.getItems());
    }

    assertThat(g.getItems().size()).isEqualTo(1);

    // If this line of code is commented out, this test case will be successfully passed
    assertThat(o.getItems().size()).isEqualTo(0);

    // org.junit.ComparisonFailure: Expected :1 Actual :2
    assertThat(g.getItems().size()).isEqualTo(1);

    assertThat(g.getItems().get(0).getName()).isEqualTo("user");
  }

}
