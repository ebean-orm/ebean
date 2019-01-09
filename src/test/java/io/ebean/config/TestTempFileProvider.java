package io.ebean.config;



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.Test;

/**
 * Verifies, that a file will not be deleted, if the application moves the file away.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestTempFileProvider {

  private void doFileMove(TempFileProvider prov) throws Exception {
    File tempFile1 = prov.createTempFile();
    String fileName1 =  tempFile1.getAbsolutePath();

    File tempFile2 = prov.createTempFile();
    String fileName2 =  tempFile2.getAbsolutePath();


    // let the application move the file away
    File keepFile = new File(tempFile1.getAbsoluteFile().getAbsolutePath()+"-keep");
    tempFile1.renameTo(keepFile);

    prov.shutdown();

    // the moved file must not be deleted
    assertTrue(keepFile.exists());
    // but both other files
    assertFalse(new File(fileName1).exists());
    assertFalse(new File(fileName2).exists());

    // cleanup
    keepFile.delete();

  }

  @Test
  public void testWeakRefMove() throws Exception {
    doFileMove(new WeakRefTempFileProvider());
  }

  @Test
  public void testDeleteShutdownMove() throws Exception {
    doFileMove(new DeleteOnShutdownTempFileProvider());
  }

}
