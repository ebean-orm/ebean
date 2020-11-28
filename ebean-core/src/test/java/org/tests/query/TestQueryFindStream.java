package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFindStream extends BaseTestCase {

  @Test
  public void findStream_basic() {

    ResetBasicData.reset();

    try (Stream<Customer> stream = DB.find(Customer.class)
      .findStream()) {

      // bad example, don't use a stream like this when we can
      // use findSingleAttributeList() instead
      final List<String> namesStream = stream
        .map(Customer::getName)
        .collect(toList());

      final List<String> namesQuery = DB.find(Customer.class)
        .select("name")
        .findSingleAttributeList();

      assertThat(namesStream).hasSize(namesQuery.size());
      assertThat(namesStream).containsAll(namesQuery);
    }
  }

  @Test
  public void findLargeStream_basic() {

    ResetBasicData.reset();

    try (Stream<Customer> stream = DB.find(Customer.class)
      .findLargeStream()) {

      // bad example, don't use a stream like this when we can
      // use findSingleAttributeList() instead
      final List<String> namesStream = stream
        .map(Customer::getName)
        .collect(toList());

      final List<String> namesQuery = DB.find(Customer.class)
        .select("name")
        .findSingleAttributeList();

      assertThat(namesStream).hasSize(namesQuery.size());
      assertThat(namesStream).containsAll(namesQuery);
    }
  }

  @Test
  public void manualTest_findSteam_when_streamNotClosed_connectionLeak() {

    Stream<Customer> stream = DB.find(Customer.class).findStream();
    // remember a steam MUST be closed or we leak resources
    // comment out the close(); below to leak a connection
    stream.close();
  }
}
