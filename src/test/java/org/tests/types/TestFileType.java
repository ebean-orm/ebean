package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;

import org.tests.model.types.SomeFileBean;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

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

  @Test
  public void test_canDeleteIfStreamIsOpen() throws IOException {
    File tempFile = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile)) {
      ps.println("Hello World!");
    }
    InputStream is = new BufferedInputStream(new FileInputStream(tempFile));
    byte[] buf = new byte[4096];
    is.read(buf);
    // if stream held open, delete will fail
    assertFalse(tempFile.delete());
    is.close();
    assertTrue(tempFile.delete());
  }

  @Test
  public void test_closeFileStreamUnbatched() throws IOException {
    File tempFile = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile)) {
      ps.println("Hello World!");
    }

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("tempBeanUnbatched");
    bean0.setContent(tempFile);
    Ebean.save(bean0);

    assertTrue(tempFile.delete());
  }

  @Test
  public void test_closeFileStreamBatched() throws IOException {
    File tempFile = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile)) {
      ps.println("Hello World!");
    }
    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchSize(30);
      txn.setBatchMode(true);

      SomeFileBean bean0 = new SomeFileBean();
      bean0.setName("tempBeanBatched");
      bean0.setContent(tempFile);
      Ebean.save(bean0);

      txn.commit();
    }

    assertTrue(tempFile.delete());

  }

  @Test
  public void test_queryFileStream() throws IOException {
    File tempFile0 = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile0)) {
      ps.println("Search String");
    }

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("bean to find");
    bean0.setContent(tempFile0);
    Ebean.save(bean0);

    File tempFile1 = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile1)) {
      ps.println("Search String");
    }

    // find all beans that have the same file content
    SomeFileBean bean1 = Ebean.find(SomeFileBean.class).where().eq("content",tempFile1).findOne();
    assertNotNull(bean1);
    assertThat(bean1.getName()).isEqualTo("bean to find");

    assertTrue(tempFile1.delete()); // check if file is hold by the query
    assertTrue(tempFile0.delete()); // check if file is hold by the inssert
  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
