package org.tests.types;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.types.SomeFileBean;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileTypeFetching extends BaseTestCase {

  File file = getFile("/profile-image.jpg");
  File file2 = getFile("/java-64.png");

  @Test
  public void test_lazyFetch_statelessUpdate() {

    assertTrue(file.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("one");
    bean0.setContent(file);
    DB.save(bean0);

    SomeFileBean bean1 = DB.find(SomeFileBean.class)
      .setId(bean0.getId())
      .findOne();

    BeanState beanState = DB.beanState(bean1);
    Set<String> loadedProps = beanState.loadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    File file1 = bean1.getContent();
    assertEquals(file.length(), file1.length());


    SomeFileBean statelessUpdateBean = new SomeFileBean();
    statelessUpdateBean.setId(bean0.getId());
    statelessUpdateBean.setContent(file2);

    // perform stateless update (handy)
    DB.update(statelessUpdateBean);

    SomeFileBean bean2 = DB.find(SomeFileBean.class)
      .select("content")
      .setId(bean0.getId())
      .findOne();

    assertEquals(file2.length(), bean2.getContent().length());

    DB.delete(bean1);
  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
