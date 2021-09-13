package org.tests.model.basic.test;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.CustDto;
import org.tests.model.basic.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NamedDtoQueryTest {

  @Test
  public void dto_findList_constructorMatch() {

    Customer customer = new Customer();
    customer.setName("dtoTest");
    customer.save();

    final List<CustDto> list = DB.getDefault().createNamedDtoQuery(CustDto.class, "findByName")
      .setParameter("dtoT%")
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getId()).isEqualTo(customer.getId());
  }
}
