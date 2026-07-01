package org.tests.types;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.types.SomeFileBean;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class TestFileType extends BaseTestCase {

  private final File file = getFile("/profile-image.jpg");
  private final File file2 = getFile("/java-64.png");

  private File newTempFile() throws IOException {
    File tempFile = File.createTempFile("testfile", "txt");
    try (PrintStream ps = new PrintStream(tempFile)) {
      ps.println("Hello World!");
    }
    return tempFile;
  }

  @Test
  public void test_closeFileStreamUnbatched() throws IOException {

    File tempFile = newTempFile();

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("tempBeanUnbatched");
    bean0.setContent(tempFile);
    DB.save(bean0);

    assertTrue(tempFile.delete());
  }

  @Test
  public void test_closeFileStreamUnbatched_onUpdate() throws IOException {

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("tempBeanUnbatched");
    bean0.setContent(newTempFile());
    DB.save(bean0);


    File updateFile = newTempFile();
    bean0.setName("tempBeanModified");
    bean0.setContent(updateFile);
    DB.save(bean0);

    assertTrue(updateFile.delete());
  }

  @Test
  public void test_closeFileStreamBatched() throws IOException {

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("tempBeanUnbatched");
    bean0.setContent(newTempFile());
    DB.save(bean0);


    File tempFile = newTempFile();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchSize(30);
      txn.setBatchMode(true);

      bean0.setName("tempBeanBatchedModified");
      bean0.setContent(tempFile);
      DB.save(bean0);

      txn.commit();
    }

    assertTrue(tempFile.delete());
  }

  @Test
  public void test_closeFileStreamBatched_update() throws IOException {

    File tempFile = newTempFile();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchSize(30);
      txn.setBatchMode(true);

      SomeFileBean bean0 = new SomeFileBean();
      bean0.setName("tempBeanBatched");
      bean0.setContent(tempFile);
      DB.save(bean0);

      txn.commit();
    }

    assertTrue(tempFile.delete());
  }

  @Test
  public void test_insertNullFile() {

    assertTrue(file.exists());
    assertTrue(file2.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    DB.save(bean0);

    SomeFileBean bean1 = DB.find(SomeFileBean.class)
      .select("name, content")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean1.getName());
    assertNull(bean1.getContent());

    bean1.setContent(file);
    DB.save(bean1);

    SomeFileBean bean2 = DB.find(SomeFileBean.class)
      .select("name, content")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean2.getName());
    assertNotNull(bean2.getContent());
    DB.delete(bean1);
  }

  @Test
  public void test_insertUpdateDelete() {

    assertTrue(file.exists());
    assertTrue(file2.exists());

    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    bean0.setContent(file);

    DB.save(bean0);

    SomeFileBean bean1 = DB.find(SomeFileBean.class)
      .select("name, content")
      .setId(bean0.getId())
      .findOne();

    assertEquals("afile", bean1.getName());
    assertNotNull(bean1.getContent());
    assertEquals(file.length(), bean1.getContent().length());

    bean1.setName("mod-file");
    bean1.setContent(file2);
    // update to file2
    DB.save(bean1);


    SomeFileBean bean2 = DB.find(SomeFileBean.class)
      .select("name, content")
      .setId(bean0.getId())
      .findOne();

    assertEquals(file2.length(), bean2.getContent().length());

    // update to null
    bean2.setContent(null);
    bean2.setName("setNull");
    DB.save(bean2);

    SomeFileBean bean3 = DB.find(SomeFileBean.class)
      .select("name, content")
      .setId(bean0.getId())
      .findOne();

    assertNull(bean3.getContent());

    bean3.setName("changeOnlyName");
    DB.save(bean3);

    DB.delete(bean3);
  }

  @Test
  void test_FileDirty() throws Exception {
    SomeFileBean bean0 = new SomeFileBean();
    bean0.setName("afile");
    bean0.setContent(file);

    DB.save(bean0);

    SomeFileBean bean1 = DB.find(SomeFileBean.class)
        .select("name, content")
        .setId(bean0.getId())
        .findOne();

    assertThat(bean1.getContent())
        .isNotEqualTo(file) // not same file object, but same content
        .hasSameBinaryContentAs(file);

    assertFalse(DB.beanState(bean1).isDirty());
    bean1.setContent(file);
    assertFalse(DB.beanState(bean1).isDirty());
    bean1.setContent(file2);
    assertTrue(DB.beanState(bean1).isDirty());

  }

  private File getFile(String resource) {
    URL url = getClass().getResource(resource);
    return new File(url.getFile());
  }

}
