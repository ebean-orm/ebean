package io.ebean.spring.boot;

import io.ebean.bean.EntityBean;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.unenhanced.Boondoggle;
import org.unenhanced.Wotsit;
import static org.assertj.core.api.StrictAssertions.assertThat;

@SpringBootTest(webEnvironment=WebEnvironment.NONE)
public class EbeanAgentAutoConfigurationTest extends AbstractJUnit4SpringContextTests {

  static {
    // Validate our test setup. Agent should not be loaded yet, so 'Boondoggle'
    // should have been enhanced neither at build time nor at load time.
    assertThat(EntityBean.class.isAssignableFrom(Boondoggle.class)).isFalse();
  }

  @Configuration
  @EnableAutoConfiguration
  public static class Config {
    /* no beans needed for this test */
  }

  @Test
  public void testAgentIsWorking() {
    // Wotsit is outside of the org.example package that's being enhanced
    // at build time, so should be picked up by the agent only.
    assertThat(EntityBean.class.isAssignableFrom(Wotsit.class)).isTrue();
  }
}
