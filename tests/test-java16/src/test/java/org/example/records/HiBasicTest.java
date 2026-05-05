package org.example.records;

import io.ebean.DB;
import org.example.records.query.QHiBasic;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HiBasicTest {

  @Test
  void insert() {

    var bean = new HiBasic();
    var map = new LinkedHashMap<String,HiMap>();
    map.put("a", new HiMap("a", "b"));
    bean.setMap(map);
    bean.setSeqs(new LinkedHashSet<>(Set.of(new HiSeq("x"))));

    DB.save(bean);

    List<HiBasic> list = new QHiBasic()
      .seqs.fetch()
      .map.fetch()
      .findList();

    assertThat(list).hasSize(1);
    HiBasic hiBasic = list.get(0);
    assertThat(hiBasic.seqs()).hasSize(1);
    assertThat(hiBasic.map()).hasSize(1);
  }
}
