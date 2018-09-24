package org.tests.quotedidentifier;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.basic.BWithQIdent;

public class TestQuotedIdentifierQuery extends BaseTestCase {

  @Test
  public void test() {

    BWithQIdent bean = new BWithQIdent();
    bean.setName("foo");
    bean.setCODE("bar");

    Ebean.save(bean);

    Ebean.find(BWithQIdent.class)
      .where()
      .eq("name", "foo")
      .raw("t0.\"Name\" = ?", "foo")
      .raw("t0.\"CODE\" = ?", "bar")
      .findList();
  }
}
