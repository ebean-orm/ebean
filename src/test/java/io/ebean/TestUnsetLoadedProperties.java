package io.ebean;

import io.ebean.bean.EntityBean;
import org.tests.model.converstation.User;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUnsetLoadedProperties extends BaseTestCase {

  @Test
  public void testUnsetPropertyLoadedState() {

    // populate db with a user
    User seed = new User();
    seed.setName("someName");
    seed.setEmail("some@junk.com");
    seed.save();


    // our bean to perform stateless update
    User user = new User();
    user.setId(seed.getId());
    user.setName("name mod");
    user.setEmail("change@junk.com");


    BeanState beanState = Ebean.getBeanState(user);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name", "email");

    user.markPropertyUnset("email");

    assertThat(beanState.getLoadedProps()).containsExactly("id", "name");

    LoggedSqlCollector.start();
    user.update();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("email");

  }

  @Test
  public void testUnloadVia_EntityBeanIntercept_setPropertyLoaded() {

    // our bean to perform stateless update
    User user = new User();
    user.setId(42L);
    user.setName("name mod");
    user.setEmail("change@junk.com");

    BeanState beanState = Ebean.getBeanState(user);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name", "email");

    // unset the loaded state for email
    ((EntityBean) user)._ebean_getIntercept().setPropertyLoaded("email", false);

    assertThat(beanState.getLoadedProps()).containsExactly("id", "name");
  }

  @Test
  public void testUnloadVia_Model_markPropertyUnset() {

    // our bean to perform stateless update
    User user = new User();
    user.setId(42L);
    user.setName("name mod");
    user.setEmail("change@junk.com");

    BeanState beanState = Ebean.getBeanState(user);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name", "email");


    user.markPropertyUnset("email");
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name");
  }

  @Test
  public void testUnloadVia_BeanState_setPropertyLoaded() {

    // our bean to perform stateless update
    User user = new User();
    user.setId(42L);
    user.setName("name mod");
    user.setEmail("change@junk.com");

    BeanState beanState = Ebean.getBeanState(user);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name", "email");

    Ebean.getBeanState(user).setPropertyLoaded("email", false);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name");
  }

}
