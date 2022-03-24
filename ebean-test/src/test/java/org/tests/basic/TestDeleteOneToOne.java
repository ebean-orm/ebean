package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.PersistentFile;
import org.tests.model.basic.PersistentFileContent;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDeleteOneToOne extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {

    PersistentFile persistentFile = new PersistentFile("test.txt", new PersistentFileContent(
      "test".getBytes(StandardCharsets.UTF_8)));

    DB.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getPersistentFileContent().getId();

    // should delete file and fileContent
    DB.delete(PersistentFile.class, id);

    PersistentFile file1 = DB.find(PersistentFile.class, id);
    PersistentFileContent content1 = DB.find(PersistentFileContent.class, contentId);

    assertNull(file1);
    assertNull(content1);
  }

  // public void testDeleteMany() {
  //
  // Customer c = new Customer();
  // c.setName("Fiona");
  // c.setStatus(Customer.Status.ACTIVE);
  // c.addContact(new Contact("Fiona", "Black"));
  // c.addContact(new Contact("Tracy", "Red"));
  //
  // DB.save(c);
  //
  // DB.delete(Customer.class, c.getId());
  //
  // Customer deletedCustomer = DB.find(Customer.class, c.getId());
  //
  // assertNull(deletedCustomer);
  //
  // }
}
