package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.types.SomeFileBean;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class TestFileType extends BaseTestCase {

  File file = getFile("/profile-image.jpg");
  File file2 = getFile("/java-64.png");

  @Test
  public void test_insertNullFile() {

    assertTrue(file.exists());
    assertTrue(file2.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    Ebean.save(bean0);

    SomeFileBean bean1 = Ebean.find(SomeFileBean.class)
      .select("name, file")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean1.getName());
    assertNull(bean1.getContent());

    bean1.setContent(file);
    Ebean.save(bean1);

    SomeFileBean bean2 = Ebean.find(SomeFileBean.class)
      .select("name, file")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean2.getName());
    assertNotNull(bean2.getContent());
    Ebean.delete(bean1);
  }

  @Test
  public void test_insertUpdateDelete() {

    assertTrue(file.exists());
    assertTrue(file2.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    bean0.setContent(file);

    Ebean.save(bean0);

    SomeFileBean bean1 = Ebean.find(SomeFileBean.class)
      .select("name, file")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean1.getName());
    assertNotNull(bean1.getContent());
    assertEquals(file.length(), bean1.getContent().length());

    bean1.setName("mod-file");
    bean1.setContent(file2);
    // update to file2
    Ebean.save(bean1);


    SomeFileBean bean2 = Ebean.find(SomeFileBean.class)
      .select("name, file")
      .setId(bean0.getId())
      .findOne();

    assertEquals(file2.length(), bean2.getContent().length());

    // update to null
    bean2.setContent(null);
    bean2.setName("setNull");
    Ebean.save(bean2);

    SomeFileBean bean3 = Ebean.find(SomeFileBean.class)
      .select("name, file")
      .setId(bean0.getId())
      .findOne();

    assertNull(bean3.getContent());

    bean3.setName("changeOnlyName");
    Ebean.save(bean3);

    Ebean.delete(bean3);
  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
