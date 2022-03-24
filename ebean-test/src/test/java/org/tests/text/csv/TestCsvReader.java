package org.tests.text.csv;

import io.ebean.DB;
import io.ebean.xtest.base.TransactionalTestCase;
import io.ebean.text.csv.CsvReader;
import io.ebean.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.Reader;
import java.net.URL;
import java.util.Locale;

public class TestCsvReader extends TransactionalTestCase {

  @Test
  public void test() throws Exception {

    ResetBasicData.reset();

    URL resource = TestCsvReaderWithCallback.class.getResource("/test1.csv");
    try (Reader reader =  IOUtils.newReader(resource.openStream())){

      CsvReader<Customer> csvReader = DB.getDefault().createCsvReader(Customer.class);

      csvReader.setPersistBatchSize(2);

      csvReader.addIgnore();
      // csvReader.addProperty("id");
      csvReader.addProperty("status");
      csvReader.addProperty("name");
      csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.ENGLISH);
      csvReader.addProperty("billingAddress.line1");
      csvReader.addProperty("billingAddress.city");
      csvReader.addProperty("billingAddress.country.code");

      csvReader.process(reader);

    }
  }

}
