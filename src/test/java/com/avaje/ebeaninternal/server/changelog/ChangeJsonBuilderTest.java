package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebean.text.json.JsonContext;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;


public class ChangeJsonBuilderTest extends BaseTestCase {

  Helper helper = new Helper();

  @Test
  public void testToJson() throws Exception {

    JsonContext jsonContext = Ebean.getDefaultServer().json();
    ChangeJsonBuilder builder = new ChangeJsonBuilder(jsonContext);

    ChangeSet changeSet = helper.createChangeSet("ABCD", 10);
    List<BeanChange> changes = changeSet.getChanges();
    for (int i = 0; i < changes.size(); i++) {
      StringWriter buffer = new StringWriter();
      builder.writeBeanJson(buffer, changes.get(i), changeSet, i);
      System.out.println(buffer.toString());
    }

  }
}