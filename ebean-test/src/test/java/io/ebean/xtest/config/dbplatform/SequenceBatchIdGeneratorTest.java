package io.ebean.xtest.config.dbplatform;

import io.ebean.config.dbplatform.SequenceIdGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;

import static java.util.Arrays.asList;

public class SequenceBatchIdGeneratorTest {

  @Test
  public void test() {

    TD generator = new TD();

    Assertions.assertThat(generator.nextId(null)).isEqualTo(1L);
    Assertions.assertThat(generator.nextId(null)).isEqualTo(2L);
    Assertions.assertThat(generator.nextId(null)).isEqualTo(3L);
    Assertions.assertThat(generator.nextId(null)).isEqualTo(4L);
    Assertions.assertThat(generator.nextId(null)).isEqualTo(5L);
    Assertions.assertThat(generator.nextId(null)).isEqualTo(6L);
  }

  private class TD extends SequenceIdGenerator {

    protected TD() {
      super(null, null, null, 10);
    }

    @Override
    public String getSql(int batchSize) {
      return "not used";
    }

    @Override
    protected List<Long> readIds(ResultSet resultSet, int loadSize) {
      // do nothing
      return null;
    }

    @Override
    protected List<Long> getMoreIds(Object tenantKey, int requestSize) {
      // simulate out of order ids returned from the database (verifies sorted polling)
      return asList(1L, 2L, 5L, 6L, 3L, 4L);
    }

    @Override
    protected void loadInBackground(int requestSize) {
      // do nothing - avoid background reload re-introducing already polled ids
    }
  }
}
