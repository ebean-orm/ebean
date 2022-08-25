package io.ebean.csv.reader;

import org.junit.jupiter.api.Test;

public class TestCsvReaderWithCallback {

  @Test
  public void test() throws Throwable {

//    URL resource = TestCsvReaderWithCallback.class.getResource("/test1.csv");
//    try (Reader reader = IOUtils.newReader(resource.openStream())) {
//
//      CsvReader<Customer> csvReader = DB.getDefault().createCsvReader(Customer.class);
//
//      csvReader.setPersistBatchSize(2);
//      csvReader.setLogInfoFrequency(3);
//
//      csvReader.addIgnore();
//      // csvReader.addProperty("id");
//      csvReader.addProperty("status");
//      csvReader.addProperty("name");
//      csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.ENGLISH);
//      csvReader.addProperty("billingAddress.line1");
//      csvReader.addProperty("billingAddress.city");
//      // processor.addReference("billingAddress.country.code");
//      csvReader.addProperty("billingAddress.country.code");
//
//      int before = DB.find(Customer.class).findCount();
//
//      csvReader.process(reader, new DefaultCsvCallback<Customer>() {
//
//        @Override
//        public void processBean(int row, String[] lineContent, Customer cust) {
//
//          server.save(cust.getBillingAddress(), transaction);
//          server.save(cust, transaction);
//
//        }
//
//      });
//
//      int after = DB.find(Customer.class).findCount();
//      Assertions.assertThat(after).isEqualTo(before + 9);
//    }
  }

}
