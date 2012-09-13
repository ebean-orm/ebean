package com.avaje.tests.text.csv;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class TestCsvReader extends TestCase {

	public void test() {

		ResetBasicData.reset();
		
		try {
			File f = new File("src/test/resources/test1.csv");

			FileReader reader = new FileReader(f);

			
			CsvReader<Customer> csvReader = Ebean.createCsvReader(Customer.class);

			csvReader.setPersistBatchSize(2);
			
			csvReader.addIgnore();
			//csvReader.addProperty("id");
			csvReader.addProperty("status");
			csvReader.addProperty("name");
			csvReader.addDateTime("anniversary", "dd-MMM-yyyy", Locale.GERMAN);
			csvReader.addProperty("billingAddress.line1");
			csvReader.addProperty("billingAddress.city");
			csvReader.addReference("billingAddress.country.code");

			
			csvReader.process(reader);
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
