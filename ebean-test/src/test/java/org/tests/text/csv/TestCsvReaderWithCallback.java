package org.tests.text.csv;

import io.ebean.DB;
import io.ebean.TransactionalTestCase;
import io.ebean.text.csv.CsvReader;
import io.ebean.text.csv.DefaultCsvCallback;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCsvReaderWithCallback extends TransactionalTestCase {

  @Test
  public void test() throws Throwable {

    URL resource = TestCsvReaderWithCallback.class.getResource("/test1.csv");
    File f = new File(resource.getFile());

    FileReader reader = new FileReader(f);

    CsvReader<Customer> csvReader = DB.getDefault().createCsvReader(Customer.class);

    csvReader.setPersistBatchSize(2);
    csvReader.setLogInfoFrequency(3);

    csvReader.addIgnore();
    // csvReader.addProperty("id");
    csvReader.addProperty("status");
    csvReader.addProperty("name");
    csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.ENGLISH);
    csvReader.addProperty("billingAddress.line1");
    csvReader.addProperty("billingAddress.city");
    // processor.addReference("billingAddress.country.code");
    csvReader.addProperty("billingAddress.country.code");

    int before = DB.find(Customer.class).findCount();

    csvReader.process(reader, new DefaultCsvCallback<Customer>() {

      @Override
      public void processBean(int row, String[] lineContent, Customer cust) {

        server.save(cust.getBillingAddress(), transaction);
        server.save(cust, transaction);

      }

    });

    int after = DB.find(Customer.class).findCount();
    assertThat(after).isEqualTo(before + 9);
  }

}
