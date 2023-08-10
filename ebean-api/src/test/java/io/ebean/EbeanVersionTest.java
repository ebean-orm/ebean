package io.ebean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbeanVersionTest {

  @Test
  void checkMinAgentVersion_ok() {
    assertFalse(EbeanVersion.checkMinAgentVersion("13.10.0"));
    assertFalse(EbeanVersion.checkMinAgentVersion("13.10.99"));
    assertFalse(EbeanVersion.checkMinAgentVersion("14.1.0"));
  }

  @Test
  void checkMinAgentVersion_agentTooOld() {
    assertTrue(EbeanVersion.checkMinAgentVersion("11.13.0"));
    assertTrue(EbeanVersion.checkMinAgentVersion("12.11.0"));
    assertTrue(EbeanVersion.checkMinAgentVersion("12.11.99"));
  }

  @Test
  void checkMinAgentVersion_unexpectedAgentVersion() {
    assertTrue(EbeanVersion.checkMinAgentVersion("13.13"));
    assertTrue(EbeanVersion.checkMinAgentVersion("13"));
    assertTrue(EbeanVersion.checkMinAgentVersion(""));
  }

}
