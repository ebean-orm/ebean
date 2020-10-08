package io.ebean.config.dbplatform;

import org.junit.Test;

import java.sql.ResultSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SequenceBatchIdGeneratorTest {

  @Test
  public void test() {

    TD generator = new TD();

    // simulate out of order adding of sequence ids
    generator.add(asList(1L, 2L));
    generator.add(asList(5L, 6L));
    generator.add(asList(3L, 4L));

    assertThat(generator.nextId(null)).isEqualTo(1L);
    assertThat(generator.nextId(null)).isEqualTo(2L);
    assertThat(generator.nextId(null)).isEqualTo(3L);
    assertThat(generator.nextId(null)).isEqualTo(4L);
    assertThat(generator.nextId(null)).isEqualTo(5L);
    assertThat(generator.nextId(null)).isEqualTo(6L);
  }

  private class TD extends SequenceIdGenerator {

    protected TD() {
      super(null, null, null, 10);
    }

    void add(List<Long> ids) {
      idList.addAll(ids);
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
    protected List<Long> getMoreIds(int requestSize) {
      return null;
    }

    @Override
    protected void loadInBackground(int requestSize) {
      // do nothing
    }
  }
}
