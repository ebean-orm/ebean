package io.ebeaninternal.server.deploy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DetermineAggPathTest {

  @Test
  public void path() throws Exception {

    Assertions.assertThat(DetermineAggPath.path("count(details)")).isEqualTo("details");
    assertThat(DetermineAggPath.path("count(details )")).isEqualTo("details");

    assertThat(DetermineAggPath.path("count(person.contacts)")).isEqualTo("person.contacts");
    assertThat(DetermineAggPath.path("sum(details.quantity*details.unitPrice)")).isEqualTo("details.quantity");
  }

  @Test
  public void paths_simple() throws Exception {

    DetermineAggPath.Path paths = DetermineAggPath.paths("count(details)");
    assertThat(paths.paths).isEqualTo(new String[]{"details"});
  }

  @Test
  public void paths_nested() throws Exception {

    DetermineAggPath.Path paths = DetermineAggPath.paths("count(person.contacts)");
    assertThat(paths.paths).isEqualTo(new String[]{"person", "contacts"});
  }

}
