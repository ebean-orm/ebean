package org.tests.expression.bitwise;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBitwiseExpressions extends BaseTestCase {

  @Test
  public void where() {

    setup();
    bitwiseNot();
    bitwiseAnd();
    bitwiseAny();
    bitwiseAll();
  }

  private void bitwiseNot() {

    List<BwBean> notColour = Ebean.find(BwBean.class)
      // HAS_COLOUR not set ...
      .where().bitwiseNot("flags", BwFlags.HAS_COLOUR)
      .findList();

    assertThat(notColour).hasSize(2);
    assertThat(notColour.get(0).getName()).isEqualTo("Nothing");
    assertThat(notColour.get(1).getName()).isEqualTo("SizeOnly");
  }

  private void bitwiseAnd() {

    List<BwBean> list = Ebean.find(BwBean.class)
      // not bulk set AND size set
      .where().bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE, BwFlags.HAS_SIZE)
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("SizeOnly");

    list = Ebean.find(BwBean.class)
      // bulk set AND not size set
      .where().bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE, BwFlags.HAS_BULK)
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("ColourAndBulk");

    list = Ebean.find(BwBean.class)
      // bulk set AND not size set
      .where().bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR, BwFlags.HAS_COLOUR)
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("ColourOnly");
  }

  private void bitwiseAny() {

    List<BwBean> list = Ebean.find(BwBean.class)
      .where().bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE)
      .findList();

    assertThat(list).hasSize(3);
    assertThat(list.get(0).getName()).isEqualTo("SizeOnly");
    assertThat(list.get(1).getName()).isEqualTo("ColourAndBulk");
    assertThat(list.get(2).getName()).isEqualTo("Everything");


    list = Ebean.find(BwBean.class)
      .where().bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE + BwFlags.HAS_COLOUR)
      .findList();

    assertThat(list).hasSize(4);

    list = Ebean.find(BwBean.class)
      .where().bitwiseAny("flags", BwFlags.HAS_SIZE + BwFlags.HAS_COLOUR)
      .findList();

    assertThat(list).hasSize(4);

    list = Ebean.find(BwBean.class)
      .where().bitwiseAny("flags", BwFlags.HAS_SIZE)
      .findList();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).getName()).isEqualTo("SizeOnly");
    assertThat(list.get(1).getName()).isEqualTo("Everything");

    list = Ebean.find(BwBean.class)
      .where().bitwiseAny("flags", BwFlags.HAS_COLOUR)
      .findList();

    assertThat(list).hasSize(3);
    assertThat(list.get(0).getName()).isEqualTo("ColourOnly");
    assertThat(list.get(1).getName()).isEqualTo("ColourAndBulk");
    assertThat(list.get(2).getName()).isEqualTo("Everything");
  }

  private void bitwiseAll() {

    List<BwBean> list = Ebean.find(BwBean.class)
      .where().bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE)
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("Everything");

    list = Ebean.find(BwBean.class)
      .where().bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
      .orderBy().asc("id")
      .findList();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).getName()).isEqualTo("ColourAndBulk");
    assertThat(list.get(1).getName()).isEqualTo("Everything");

    list = Ebean.find(BwBean.class)
      .where().bitwiseAll("flags", BwFlags.HAS_SIZE)
      .orderBy().asc("id")
      .findList();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).getName()).isEqualTo("SizeOnly");
    assertThat(list.get(1).getName()).isEqualTo("Everything");

    list = Ebean.find(BwBean.class)
      .where().bitwiseAll("flags", BwFlags.HAS_COLOUR)
      .orderBy().asc("id")
      .findList();

    assertThat(list).hasSize(3);
    assertThat(list.get(0).getName()).isEqualTo("ColourOnly");
    assertThat(list.get(1).getName()).isEqualTo("ColourAndBulk");
    assertThat(list.get(2).getName()).isEqualTo("Everything");

  }

  private void setup() {

    Ebean.find(BwBean.class).delete();

    new BwBean("Nothing", BwFlags.NOTHING).save();
    new BwBean("ColourOnly", BwFlags.HAS_COLOUR).save();
    new BwBean("SizeOnly", BwFlags.HAS_SIZE).save();
    new BwBean("ColourAndBulk", BwFlags.HAS_COLOUR + BwFlags.HAS_BULK).save();
    new BwBean("Everything", BwFlags.HAS_COLOUR + BwFlags.HAS_BULK + BwFlags.HAS_SIZE).save();

  }
}
