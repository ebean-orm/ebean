package io.ebean.xtest.base;

import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoQuery3Test extends BaseTestCase {

  @Test
  void dtoQuery_when_constructorsClash() {
    ResetBasicData.reset();

    List<DCust> list = server().findDto(DCust.class, "select id, name, status from o_customer where status is not null").findList();

    assertThat(list).isNotEmpty();
    for (DCust cust: list) {
      assertThat(cust.id()).isNotNull();
      assertThat(cust.name()).isNotNull();
      assertThat(cust.status()).isNotNull();
    }
  }

  public static class DCust {

    final Integer id;
    String name;

    String status;

    public DCust(Integer id) {
      this.id = id;
    }

    /** Constructor with 3 args - clashes with the one below */
    public DCust(Integer id, String name, List<String> notUsed) {
      this.id = id;
      this.name = name;
    }

    /** Constructor with 3 args - clashes with the one above */
    public DCust(Integer id, String name, String status) {
      this.id = id;
      this.name = name;
      this.status = status;
    }

    @Override
    public String toString() {
      return "id:" + id + " name:" + name;
    }

    public Integer id() {
      return id;
    }

    public String name() {
      return name;
    }

    public void name(String name) {
      this.name = name;
    }

    public String status() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}
