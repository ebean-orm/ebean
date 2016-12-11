package io.ebeaninternal.server.core.bootup;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DistillPackagesTest {

  @Test
  public void when_unique_expect_all() throws Exception {

    List<String> distill = DistillPackages.distill(group("one", "two"), group("three"));
    assertThat(distill).containsExactly("one", "three", "two");
  }

  @Test
  public void when_sub_expect_distilled() throws Exception {

    List<String> distill = DistillPackages.distill(group("one", "one.sub"), group("three"));
    assertThat(distill).containsExactly("one", "three");
  }


  @Test
  public void when_sub_expect_distilled2() throws Exception {

    List<String> distill = DistillPackages.distill(group("one", "one.sub"), group("one.foo"));
    assertThat(distill).containsExactly("one");
  }

  @Test
  public void when_subDotSub_expect_distilled2() throws Exception {

    List<String> distill = DistillPackages.distill(group("one", "one.sub.me"), group("two"));
    assertThat(distill).containsExactly("one", "two");
  }

  @Test
  public void when_unordered_expect_naturalOrder() throws Exception {

    List<String> distill = DistillPackages.distill(group("z.x.y", "two"), group("one", "one.sub.me"));
    assertThat(distill).containsExactly("one", "two", "z.x.y");
  }

  List<String> group(String... packages) {
    return Arrays.asList(packages);
  }

}
