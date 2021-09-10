package io.ebeaninternal.server.changelog;

import io.ebean.BaseTestCase;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ChangeJsonBuilderTest extends BaseTestCase {

  Helper helper = new Helper();

  @Test
  public void testToJson() throws Exception {

    ChangeJsonBuilder builder = new ChangeJsonBuilder();

    ChangeSet changeSet = helper.createChangeSet("ABCD", 10);
    changeSet.getUserContext().put("altUser", "role user");

    List<BeanChange> changes = changeSet.getChanges();
    for (int i = 0; i < changes.size(); i++) {
      StringWriter buffer = new StringWriter();
      builder.writeBeanJson(buffer, changes.get(i), changeSet);

      assertThat(buffer.toString()).contains("\"source\"");
      assertThat(buffer.toString()).contains("\"userId\"");
      assertThat(buffer.toString()).contains("\"userIpAddress\"");
      assertThat(buffer.toString()).contains("\"userContext\"");
    }

  }
}
