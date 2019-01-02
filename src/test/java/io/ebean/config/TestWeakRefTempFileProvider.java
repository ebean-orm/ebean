package io.ebean.config;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the WeakRefTempFileProvider. (Note: this test relies on an aggressive garbage collection.
 * if GC implementation will change, the test may fail)
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestWeakRefTempFileProvider {

  WeakRefTempFileProvider prov = new WeakRefTempFileProvider();

  @After
  public void shutdown() {
    prov.shutdown();
  }

  /**
   * Run the garbage collection and delete stale files.
   */
  private void gc() throws InterruptedException {
    System.gc();
    Thread.sleep(100);
    prov.deleteStaleTempFiles();
  }

  @Test
  public void testStaleEntries() throws Exception {
    File tempFile = prov.createTempFile();
    String fileName =  tempFile.getAbsolutePath();

    gc();

    assertThat(new File(fileName)).exists();

    tempFile = null; // give up reference

    gc();

    assertThat(new File(fileName)).doesNotExist();


  }

  @Test
  public void testLinkedListForward() throws Exception {
    File tempFile1 = prov.createTempFile();
    String fileName1 = tempFile1.getAbsolutePath();
    File tempFile2 = prov.createTempFile();
    String fileName2 = tempFile2.getAbsolutePath();
    File tempFile3 = prov.createTempFile();
    String fileName3 = tempFile3.getAbsolutePath();

    assertThat(new File(fileName1)).exists();
    assertThat(new File(fileName2)).exists();
    assertThat(new File(fileName3)).exists();

    gc();

    // give up first ref
    tempFile1 = null;

    gc();

    assertThat(new File(fileName1)).doesNotExist();
    assertThat(new File(fileName2)).exists();
    assertThat(new File(fileName3)).exists();

    // give up second ref
    tempFile2 = null;

    gc();

    assertThat(new File(fileName1)).doesNotExist();
    assertThat(new File(fileName2)).doesNotExist();
    assertThat(new File(fileName3)).exists();

    // give up third ref
    tempFile3 = null;

    gc();

    assertThat(new File(fileName1)).doesNotExist();
    assertThat(new File(fileName2)).doesNotExist();
    assertThat(new File(fileName3)).doesNotExist();

  }


  @Test
  public void testLinkedListReverse() throws Exception {
    File tempFile1 = prov.createTempFile();
    String fileName1 = tempFile1.getAbsolutePath();
    File tempFile2 = prov.createTempFile();
    String fileName2 = tempFile2.getAbsolutePath();
    File tempFile3 = prov.createTempFile();
    String fileName3 = tempFile3.getAbsolutePath();

    assertThat(new File(fileName1)).exists();
    assertThat(new File(fileName2)).exists();
    assertThat(new File(fileName3)).exists();

    gc();

    // give up third ref
    tempFile3 = null;

    gc();

    assertThat(new File(fileName1)).exists();
    assertThat(new File(fileName2)).exists();
    assertThat(new File(fileName3)).doesNotExist();

    // give up second ref
    tempFile2 = null;

    gc();

    assertThat(new File(fileName1)).exists();
    assertThat(new File(fileName2)).doesNotExist();
    assertThat(new File(fileName3)).doesNotExist();

    // give up first ref
    tempFile1 = null;

    gc();

    assertThat(new File(fileName1)).doesNotExist();
    assertThat(new File(fileName2)).doesNotExist();
    assertThat(new File(fileName3)).doesNotExist();

  }

  @Test
  @Ignore("Runs on Windows only")
  public void testFileLocked() throws Exception {
    File tempFile = prov.createTempFile();
    String fileName = tempFile.getAbsolutePath();

    try (FileOutputStream os = new FileOutputStream(fileName)) {
      FileLock lock = os.getChannel().lock();
      try {
        os.write(42);

        tempFile = null;
        gc();
      } finally {
        lock.release();
      }

    }

    assertThat(new File(fileName)).exists();

    prov.shutdown();

    assertThat(new File(fileName)).doesNotExist();
  }
}
