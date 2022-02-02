package org.tests.quotedidentifier;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.BWithQIdent;

public class TestQuotedIdentifierQuery extends BaseTestCase {

  @Test
  public void test() {

    BWithQIdent bean = new BWithQIdent();
    bean.setName("foo");
    bean.setCODE("bar");

    DB.save(bean);

    if (isMySql()) {

      DB.find(BWithQIdent.class)
        .where()
        .eq("name", "foo")
        .raw("t0.`Name` = ?", "foo")
        .raw("t0.`CODE` = ?", "bar")
        .findList();

    } else if (isH2() || isPostgresCompatible()) {

      DB.find(BWithQIdent.class)
        .where()
        .eq("name", "foo")
        .raw("t0.\"Name\" = ?", "foo")
        .raw("t0.\"CODE\" = ?", "bar")
        .findList();

    }
  }
}
