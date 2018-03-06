package org.tests.ddl;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestForeignKeyModes extends BaseTestCase {

  @Test
  public void none() {

    DfkOne one = new DfkOne("one");
    Ebean.save(one);

    DfkNone none = new DfkNone("none", one);
    Ebean.save(none);

    // fails unless there is no Foreign key ...
    Ebean.delete(one);

    DfkNone found = Ebean.find(DfkNone.class, none.getId());
    assertThat(found).isNotNull();
    // we still reference one ... even though it does not exist anymore
    assertThat(found.getOne()).isNotNull();
  }


  @Test
  public void noneViaJoin() {

    DfkOne one = new DfkOne("one2");
    Ebean.save(one);

    DfkNoneViaJoin none = new DfkNoneViaJoin("none2", one);
    Ebean.save(none);

    // fails unless there is no Foreign key ...
    Ebean.delete(one);

    DfkNoneViaJoin found = Ebean.find(DfkNoneViaJoin.class, none.getId());
    assertThat(found).isNotNull();
    // we still reference one ... even though it does not exist anymore
    assertThat(found.getOne()).isNotNull();
  }

  @Test
  public void setNullOnDelete() {

    DfkOne one = new DfkOne("one2");
    Ebean.save(one);

    DfkSetNull other = new DfkSetNull("none", one);
    Ebean.save(other);

    // success with ... fkey value set to null
    Ebean.delete(one);

    DfkSetNull found = Ebean.find(DfkSetNull.class, other.getId());
    assertThat(found).isNotNull();
    assertThat(found.getOne()).isNull();

  }

  @Test
  public void onDeleteCascade() {

    DfkCascadeOne one = new DfkCascadeOne("one3");


    DfkCascade other = new DfkCascade("cascade1", one);
    one.getDetails().add(other);
    one.getDetails().add(new DfkCascade("cascade2", one));
    one.getDetails().add(new DfkCascade("cascade3", one));

    Ebean.save(one);


    LoggedSqlCollector.start();
    Ebean.delete(one);

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from dfk_cascade_one where id=?");

    DfkCascade found = Ebean.find(DfkCascade.class, other.getId());
    assertThat(found).isNull();

  }
}
