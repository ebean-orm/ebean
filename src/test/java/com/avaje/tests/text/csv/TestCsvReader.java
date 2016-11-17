package com.avaje.tests.text.csv;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Locale;

public class TestCsvReader extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    try {
      URL resource = TestCsvReaderWithCallback.class.getResource("/test1.csv");
      File f = new File(resource.getFile());

      FileReader reader = new FileReader(f);

      CsvReader<Customer> csvReader = Ebean.createCsvReader(Customer.class);

      csvReader.setPersistBatchSize(2);

      csvReader.addIgnore();
      // csvReader.addProperty("id");
      csvReader.addProperty("status");
      csvReader.addProperty("name");
      csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.GERMAN);
      csvReader.addProperty("billingAddress.line1");
      csvReader.addProperty("billingAddress.city");
      csvReader.addProperty("billingAddress.country.code");

      csvReader.process(reader);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
