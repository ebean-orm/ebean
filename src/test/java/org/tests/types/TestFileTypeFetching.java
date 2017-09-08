package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.tests.model.types.SomeFileBean;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.*;

public class TestFileTypeFetching extends BaseTestCase {

  File file = getFile("/profile-image.jpg");
  File file2 = getFile("/java-64.png");

  @Test
  public void test_lazyFetch_statelessUpdate() {

    assertTrue(file.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("one");
    bean0.setContent(file);
    Ebean.save(bean0);

    SomeFileBean bean1 = Ebean.find(SomeFileBean.class)
      .setId(bean0.getId())
      .findOne();

    BeanState beanState = Ebean.getBeanState(bean1);
    Set<String> loadedProps = beanState.getLoadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    File file1 = bean1.getContent();
    assertEquals(file.length(), file1.length());


    SomeFileBean statelessUpdateBean = new SomeFileBean();
    statelessUpdateBean.setId(bean0.getId());
    statelessUpdateBean.setContent(file2);

    // perform stateless update (handy)
    Ebean.update(statelessUpdateBean);

    SomeFileBean bean2 = Ebean.find(SomeFileBean.class)
      .select("file")
      .setId(bean0.getId())
      .findOne();

    assertEquals(file2.length(), bean2.getContent().length());

    Ebean.delete(bean1);
  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
