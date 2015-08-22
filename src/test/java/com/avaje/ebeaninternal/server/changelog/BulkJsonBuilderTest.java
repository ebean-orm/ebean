package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import org.junit.Test;

import java.io.StringWriter;


public class BulkJsonBuilderTest extends BaseTestCase {

  Helper helper = new Helper();

  @Test
  public void testToJson() throws Exception {

    JsonContext jsonContext = Ebean.getDefaultServer().json();
    BulkJsonBuilder builder = new BulkJsonBuilder(jsonContext, "changelog2", "changelog2");


    StringWriter buffer = new StringWriter();
    builder.writeJson(helper.createChangeSet("ABCD", 10), buffer);
    System.out.println(buffer.toString());

    buffer = new StringWriter();
    builder.writeJson(helper.createChangeSet("ABCD-2", 15), buffer);
    System.out.println(buffer.toString());
  }
}