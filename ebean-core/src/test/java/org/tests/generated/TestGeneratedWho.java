package org.tests.generated;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.MyCurrentUserProvider;
import org.tests.model.EWhoProps;
import org.junit.Test;
import org.tests.model.generated.User;
import org.tests.model.generated.WhoPropsOneToMany;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestGeneratedWho extends BaseTestCase {

  @Test
  public void insertUpdate() {

    EWhoProps bean = new EWhoProps();
    bean.setName("one");

    MyCurrentUserProvider.setUser("INSERT_WHO_1");
    Ebean.save(bean);

    assertEquals("one", bean.getName());
    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("INSERT_WHO_1", bean.getWhoModified());
    assertNotNull(bean.getWhenCreated());
    assertNotNull(bean.getWhenModified());

    MyCurrentUserProvider.setUser("UPDATE_WHO_1");
    bean.setName("two");
    Ebean.save(bean);

    assertEquals("two", bean.getName());
    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("UPDATE_WHO_1", bean.getWhoModified());

    MyCurrentUserProvider.setUser("UPDATE_WHO_2");
    bean.setName("three");
    Ebean.save(bean);

    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("UPDATE_WHO_2", bean.getWhoModified());

    MyCurrentUserProvider.resetToDefault();
  }

  @Test
  public void insertOneToManyWhoCreated() {
    User creator = new User("jack");
    User maintainer01 = new User("jill");
    User maintainer02 = new User("joe");
    Ebean.save(creator);
    Ebean.save(maintainer01);
    Ebean.save(maintainer02);

    WhoPropsOneToMany bean = new WhoPropsOneToMany();
    bean.setName("one");

    MyCurrentUserProvider.setUser(creator);
    Ebean.save(bean);

    assertNotNull(bean.getWhoCreated());
    assertEquals(creator.getId(), bean.getWhoCreated().getId());
    assertEquals(creator.getId(), bean.getWhoModified().getId());

    MyCurrentUserProvider.setUser(maintainer01);
    bean.setName("two");
    Ebean.save(bean);

    assertEquals("two", bean.getName());
    assertEquals(creator.getId(), bean.getWhoCreated().getId());
    assertEquals(maintainer01.getId(), bean.getWhoModified().getId());

    MyCurrentUserProvider.setUser(maintainer02);
    bean.setName("three");
    Ebean.save(bean);

    assertEquals(creator.getId(), bean.getWhoCreated().getId());
    assertEquals(maintainer02.getId(), bean.getWhoModified().getId());

    MyCurrentUserProvider.resetToDefault();
  }

  @Test
  public void insertNullIntoOneToManyWhoCreated() {
    User creator = new User("jack");
    Ebean.save(creator);

    WhoPropsOneToMany bean = new WhoPropsOneToMany();
    bean.setName("one");
    bean.setWhoCreated(creator);
    bean.setWhoModified(creator);

    MyCurrentUserProvider.setUser(null);
    Ebean.save(bean);

    assertNull(bean.getWhoCreated());
    assertNull(bean.getWhoModified());

    MyCurrentUserProvider.resetToDefault();
  }
}
