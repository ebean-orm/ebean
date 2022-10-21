package io.ebean.csv.reader;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.util.IOUtils;
import org.example.Country;
import org.example.Customer;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class CsvReaderTest {

  final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

  @Test
  void test() throws Exception {

    Country country = new Country();
    country.code("NZ");
    DB.save(country);

    Country au = new Country();
    au.code("AU");
    DB.save(au);

    URL resource = TestCsvReaderWithCallback.class.getResource("/test1.csv");
    try (Reader reader = IOUtils.newReader(resource.openStream())) {

      Database database = DB.getDefault();
      CsvReader<Customer> csvReader = new CsvReader<>(database, Customer.class);

      csvReader.setPersistBatchSize(2);

      csvReader.addIgnore();
      csvReader.addProperty("status");
      csvReader.addProperty("name");

      // supplier a StringParser for custom date, time, dateTime formats
      csvReader.addProperty("anniversary", (String content) -> LocalDate.parse(content, dateFormatter));
      // OLD WAY: csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.ENGLISH);
      csvReader.addProperty("billingAddress.line1");
      csvReader.addProperty("billingAddress.city");
      csvReader.addProperty("billingAddress.country.code");

      csvReader.process(reader);

    }
  }

}
