package org.tests.model.onetoone.album;


import io.ebean.BaseTestCase;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneHardDelete extends BaseTestCase {

  @Test
  public void test() {

    Cover cover = new Cover();
    cover.setS3Url("http://foo");
    cover.save();

    Album album = new Album();
    album.setName("BlackWhite");
    album.setCover(cover);
    album.save();

    Album found1 = Album.find.byId(album.getId());

    LoggedSql.start();

    // ---------------------
    // SOFT DELETE
    // ---------------------
    found1.delete();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    // assert we loaded the missing/unloaded foreign key
    assertThat(trimSql(sql.get(0), 1)).contains("select t0.id, t0.cover_id from album t0 where t0.id = ?");
    // assert soft delete cascaded
    assertSql(sql.get(1)).contains("update album set deleted=?, last_update=? where id=?");
    assertSql(sql.get(2)).contains("update cover set deleted=? where id=?");

    Album found2 = Album.find.query().setId(album.getId()).setIncludeSoftDeletes().findOne();

    LoggedSql.start();

    // ---------------------
    // HARD DELETE
    // ---------------------
    found2.deletePermanent();

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    // assert we loaded the missing/unloaded foreign key
    assertThat(trimSql(sql.get(0), 1)).contains("select t0.id, t0.cover_id from album t0 where t0.id = ?");
    // assert hard delete cascaded
    assertSql(sql.get(1)).contains("delete from album where");
    assertSql(sql.get(2)).contains("delete from cover where");
  }
}
