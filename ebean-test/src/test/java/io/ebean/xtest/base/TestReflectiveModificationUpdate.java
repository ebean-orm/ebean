package io.ebean.xtest.base;

import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.converstation.User;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestReflectiveModificationUpdate extends BaseTestCase {

  @Test
  void statelessUpdate_via_reflection() throws NoSuchFieldException, IllegalAccessException {
    // populate db with a user
    User seed = new User();
    seed.setName("someName");
    seed.setEmail("some@junk.com");
    seed.save();

    Field field = User.class.getDeclaredField("email");
    field.setAccessible(true);

    // our bean to perform stateless update
    User user = new User();
    user.setId(seed.getId());
    user.setName("mod");

    field.set(user, "change@junk.com");

    // need to set property loaded state to include in the stateless update
    BeanState beanState = DB.beanState(user);
    beanState.setPropertyLoaded("email", true);

    LoggedSql.start();
    user.update();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update c_user set name=?, email=?, when_modified=? where id=?");
  }

  @Test
  void normalUpdate_via_reflection() throws NoSuchFieldException, IllegalAccessException {
    // populate db with a user
    User seed = new User();
    seed.setName("someName");
    seed.setEmail("some@junk.com");
    seed.save();

    Field field = User.class.getDeclaredField("email");
    field.setAccessible(true);

    // fetching the bean from database, so a "normal update"
    User user = DB.find(User.class, seed.getId());
    user.setName("mod");

    // ideally we get the old value first (if there are change listeners etc)
    Object oldValue = field.get(user);

    // reflectively modify
    field.set(user, "change@junk.com");

    // BeanState beanState = DB.beanState(user);
    // need to use EntityBeanIntercept rather than BeanState
    // so this isn't great !!
    EntityBean eb = (EntityBean) user;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    int pos = ebi.findProperty("email");

    // EITHER ideally mark as dirty with the original value
    //ebi.setChangedPropertyValue(pos, true, oldValue);

    // OR just mark as changed
    ebi.markPropertyAsChanged(pos);

    LoggedSql.start();
    user.update();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update c_user set name=?, email=?, version=?, when_modified=? where id=? and version=?");
  }
}
