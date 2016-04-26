package com.avaje.tests.model.onetoone.album;


import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneHardDelete {

  @Test
  public void test() {

    Cover cover = new Cover();
    cover.setS3Url("http://foo");
    cover.save();

    Album album = new Album();
    album.setName("BlackWhite");
    album.setCover(cover);
    album.save();

    Album found1 = Album.find.where().findUnique();

    LoggedSqlCollector.start();

    // ---------------------
    // SOFT DELETE
    // ---------------------
    found1.delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(3);
    // assert we loaded the missing/unloaded foreign key
    assertThat(sql.get(0)).contains("select t0.id c0, t0.cover_id c1 from album t0 where t0.id = ?");
    // assert soft delete cascaded
    assertThat(sql.get(1)).contains("update album set deleted=?, last_update=? where id=?");
    assertThat(sql.get(2)).contains("update cover set deleted=? where id=?");

    Album found2 = Album.find.where().setIncludeSoftDeletes().findUnique();

    LoggedSqlCollector.start();

    // ---------------------
    // HARD DELETE
    // ---------------------
    found2.deletePermanent();

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(3);
    // assert we loaded the missing/unloaded foreign key
    assertThat(sql.get(0)).contains("select t0.id c0, t0.cover_id c1 from album t0 where t0.id = ?");
    // assert hard delete cascaded
    assertThat(sql.get(1)).contains("delete from album where");
    assertThat(sql.get(2)).contains("delete from cover where");
  }
}
