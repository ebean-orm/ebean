package org.tests.ddl;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestForeignKeyModes extends BaseTestCase {

  @Test
  public void none() {

    DfkOne one = new DfkOne("one");
    DB.save(one);

    DfkNone none = new DfkNone("none", one);
    DB.save(none);

    // fails unless there is no Foreign key ...
    DB.delete(one);

    DfkNone found = DB.find(DfkNone.class, none.getId());
    assertThat(found).isNotNull();
    // we still reference one ... even though it does not exist anymore
    assertThat(found.getOne()).isNotNull();
  }


  @Test
  public void noneViaJoin() {

    DfkOne one = new DfkOne("one2");
    DB.save(one);

    DfkNoneViaJoin none = new DfkNoneViaJoin("none2", one);
    DB.save(none);

    // fails unless there is no Foreign key ...
    DB.delete(one);

    DfkNoneViaJoin found = DB.find(DfkNoneViaJoin.class, none.getId());
    assertThat(found).isNotNull();
    // we still reference one ... even though it does not exist anymore
    assertThat(found.getOne()).isNotNull();
  }

  @Test
  public void noneViaManyToMany() {

    DfkOne one = DB.reference(DfkOne.class, 999L);

    DfkNoneViaMtoM none = new DfkNoneViaMtoM("none2");
    none.getOnes().add(one);

    // Would normally fail as DfkOne id:999 is not actually in Database
    // and with the foreign key constraint on the intersection table the
    // insert into the intersection table would fail
    DB.save(none);

    DfkNoneViaMtoM found = DB.find(DfkNoneViaMtoM.class)
      .setId(none.getId())
      .fetch("ones", "id")
      .findOne();

    assertThat(found).isNotNull();

    // but we can't actually fetch it via ORM M2M ... as it joins across
    // to DfkOne and that doesn't exist
    assertThat(found.getOnes()).isNotNull();
    assertThat(found.getOnes()).hasSize(0);

    LoggedSql.start();
    DB.delete(none);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from dfk_none_via_mto_m_dfk_one where dfk_none_via_mto_m_id = ?");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(2)).contains("delete from dfk_none_via_mto_m where id=?");
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void setNullOnDelete() {

    DfkOne one = new DfkOne("one2");
    DB.save(one);

    DfkSetNull other = new DfkSetNull("none", one);
    DB.save(other);

    // success with ... fkey value set to null
    DB.delete(one);

    DfkSetNull found = DB.find(DfkSetNull.class, other.getId());
    assertThat(found).isNotNull();
    assertThat(found.getOne()).isNull();
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void onDeleteCascade() {

    DfkCascadeOne one = new DfkCascadeOne("one3");


    DfkCascade other = new DfkCascade("cascade1", one);
    one.getDetails().add(other);
    one.getDetails().add(new DfkCascade("cascade2", one));
    one.getDetails().add(new DfkCascade("cascade3", one));

    DB.save(one);


    LoggedSql.start();
    DB.delete(one);

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("delete from dfk_cascade_one where id=?");

    DfkCascade found = DB.find(DfkCascade.class, other.getId());
    assertThat(found).isNull();

  }
}
