package org.tests.cache.embeddedid;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class TestCompositeForeignKey extends BaseTestCase {

  @Test
  void createConnectionWithCompositeForeignKey() {
    String networkId = "test-network";
    Concept concept1 = new Concept(networkId, "concept1", "concept 1");
    Concept concept2 = new Concept(networkId, "concept2", "concept 2");

    DB.saveAll(concept1, concept2);

    String connectionId = "test-connection";

    Connection connection = new Connection(
      networkId, connectionId, "test", concept1, concept2
    );
    DB.save(connection);

    Connection reloaded = DB.find(Connection.class, new ConceptId(networkId, connectionId));
    Objects.requireNonNull(reloaded);

    LoggedSql.start();
    // this breaks if we uncomment the OneToMany relationships with cascade = ALL
    DB.createQuery(Concept.class).delete();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("select t0.network_id, t0.id from concept t0");
    if (isSqlServer() || isDb2() || isNuoDb()) {
      assertThat(sql.get(1)).contains("delete from connection where ");
      assertThat(sql.get(2)).contains("delete from connection where ");
      assertThat(sql.get(3)).contains("delete from concept where ");
    } else {
      assertThat(sql.get(1)).contains("delete from connection where (network_id,from_conc) in");
      assertThat(sql.get(2)).contains("delete from connection where (network_id,to_conc) in");
      assertThat(sql.get(3)).contains("delete from concept where (network_id,id)  in");
    }
  }

  @Test
  void insert() {
    var c2 = new Concept2("A", "B", "foo");
    DB.save(c2);
  }
}
