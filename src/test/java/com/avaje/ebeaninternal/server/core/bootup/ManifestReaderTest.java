package com.avaje.ebeaninternal.server.core.bootup;

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

  private Set<String> readMf(String path) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return ManifestReader.readManifests(classLoader, path);
  }

}