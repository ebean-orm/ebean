package com.avaje.tests.types;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.types.SomeFileBean;
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
            .findUnique();

    assertEquals("afile", bean1.getName());
    assertNull(bean1.getFile());

    bean1.setFile(file);
    Ebean.save(bean1);

    SomeFileBean bean2 = Ebean.find(SomeFileBean.class)
            .select("name, file")
            .setId(bean0.getId())
            .findUnique();

    assertEquals("afile", bean2.getName());
    assertNotNull(bean2.getFile());
    System.out.println("test_insertNullFile: bean2: "+bean2.getFile());

    Ebean.delete(bean1);
  }

    @Test
  public void test_insertUpdateDelete() {

    assertTrue(file.exists());
    assertTrue(file2.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    bean0.setFile(file);

    Ebean.save(bean0);

    SomeFileBean bean1 = Ebean.find(SomeFileBean.class)
            .select("name, file")
            .setId(bean0.getId())
            .findUnique();

    assertEquals("afile", bean1.getName());
    assertNotNull(bean1.getFile());
    assertEquals(file.length(), bean1.getFile().length());
    System.out.println("t2 bean1: " + bean1.getFile().getAbsoluteFile());

    bean1.setName("mod-file");
    bean1.setFile(file2);
    // update to file2
    Ebean.save(bean1);


    SomeFileBean bean2 = Ebean.find(SomeFileBean.class)
            .select("name, file")
            .setId(bean0.getId())
            .findUnique();

    assertEquals(file2.length(), bean2.getFile().length());
    System.out.println("t2 bean3: " + bean2.getFile().getAbsoluteFile());

    // update to null
    bean2.setFile(null);
    bean2.setName("setNull");
    Ebean.save(bean2);

    SomeFileBean bean3 = Ebean.find(SomeFileBean.class)
            .select("name, file")
            .setId(bean0.getId())
            .findUnique();

    assertNull(bean3.getFile());
    System.out.println("t2 bean3: " + bean3.getFile());

    bean3.setName("changeOnlyName");
    Ebean.save(bean3);

    Ebean.delete(bean3);
  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
