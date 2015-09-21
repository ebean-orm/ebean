package com.avaje.ebean;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.converstation.User;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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


    // confirm it's loaded state
    EntityBean eb = (EntityBean)user;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    int namePosition = ebi.findProperty("name");
    assertTrue(ebi.isLoadedProperty(namePosition));

    int emailPosition = ebi.findProperty("email");
    assertTrue(ebi.isLoadedProperty(emailPosition));

    BeanState beanState = Ebean.getBeanState(user);
    assertThat(beanState.getLoadedProps()).containsExactly("id", "name", "email");

    // unset the loaded state for email
    //ebi.setPropertyUnloaded(emailPosition);
    unloadProperty(user, "email");

    assertThat(beanState.getLoadedProps()).containsExactly("id", "name");

    LoggedSqlCollector.start();
    user.update();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("email");

  }

  private void unloadProperty(Object entityBean, String propertyName) {

    EntityBean eb = (EntityBean)entityBean;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    int position = ebi.findProperty(propertyName);
    if (position == -1) {
      throw new RuntimeException("Property "+propertyName+" not found on bean "+entityBean.getClass());
    }
    ebi.setPropertyUnloaded(position);
  }


}
