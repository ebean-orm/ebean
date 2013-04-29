package com.avaje.tests.text.csv;

import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.ebean.text.csv.DefaultCsvCallback;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestCsvReaderWithCallback extends BaseTestCase {

  @Test
  public void test() throws Throwable {

    ResetBasicData.reset();

    File f = new File("src/test/resources/test1.csv");

    FileReader reader = new FileReader(f);

    final EbeanServer server = Ebean.getServer(null);

    CsvReader<Customer> csvReader = server.createCsvReader(Customer.class);

    csvReader.setPersistBatchSize(2);
    csvReader.setLogInfoFrequency(3);

    csvReader.addIgnore();
    // csvReader.addProperty("id");
    csvReader.addProperty("status");
    csvReader.addProperty("name");
    csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.GERMAN);
    csvReader.addProperty("billingAddress.line1");
    csvReader.addProperty("billingAddress.city");
    // processor.addReference("billingAddress.country.code");
    csvReader.addProperty("billingAddress.country.code");

    csvReader.process(reader, new DefaultCsvCallback<Customer>() {

      @Override
      public void processBean(int row, String[] lineContent, Customer cust) {

        System.out.println(row + "> " + cust + " " + cust.getBillingAddress());

        server.save(cust.getBillingAddress(), transaction);
        server.save(cust, transaction);
      }

    });

  }

}
