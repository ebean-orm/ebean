package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.PersistentFile;
import org.tests.model.basic.PersistentFileContent;
import org.junit.Assert;
import org.junit.Test;

public class TestDeleteOneToOne extends BaseTestCase {

  @Test
  public void testCreateDeletePersistentFile() {

    PersistentFile persistentFile = new PersistentFile("test.txt", new PersistentFileContent(
      "test".getBytes()));

    Ebean.save(persistentFile);
    Integer id = persistentFile.getId();
    Integer contentId = persistentFile.getPersistentFileContent().getId();

    // should delete file and fileContent
    Ebean.delete(PersistentFile.class, id);

    PersistentFile file1 = Ebean.find(PersistentFile.class, id);
    PersistentFileContent content1 = Ebean.find(PersistentFileContent.class, contentId);

    Assert.assertNull(file1);
    Assert.assertNull(content1);

  }

  // public void testDeleteMany() {
  //
  // Customer c = new Customer();
  // c.setName("Fiona");
  // c.setStatus(Customer.Status.ACTIVE);
  // c.addContact(new Contact("Fiona", "Black"));
  // c.addContact(new Contact("Tracy", "Red"));
  //
  // Ebean.save(c);
  //
  // Ebean.delete(Customer.class, c.getId());
  //
  // Customer deletedCustomer = Ebean.find(Customer.class, c.getId());
  //
  // Assert.assertNull(deletedCustomer);
  //
  // }
}
