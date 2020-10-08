package io.ebeaninternal.server.core.bootup;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ManifestReaderTest {

  @Test
  public void readOne() throws Exception {

    Set<String> packageSet = readMf("META-INF/test/test-one.mf");
    assertThat(packageSet).contains("foo");
  }

  @Test
  public void readSome() throws Exception {

    Set<String> packageSet = readMf("META-INF/test/test-some.mf");
    assertThat(packageSet).contains("com.foo.domain", "com.bar.domain");
  }

  @Test
  public void readEntityPackages() throws Exception {

    Set<String> packageSet = readMf("META-INF/test/test-entity-packages.mf");
    assertThat(packageSet).contains("com.baz", "org.bax.domain");
  }

  @Test
  public void readCombined() throws Exception {

    Set<String> packageSet = readMf("META-INF/test/test-combined.mf");
    assertThat(packageSet).contains("com.foo.domain", "com.bar.domain", "com.baz.domain");
  }

  @Test
  public void readAgentOnlyUse() throws Exception {

    Set<String> packageSet = readMf("META-INF/test/test-agent-only-use.mf");
    assertThat(packageSet).isEmpty();
  }

  private Set<String> readMf(String path) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return ManifestReader.create(classLoader)
      .read(path)
      .entityPackages();
  }

}
