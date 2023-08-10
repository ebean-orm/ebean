package org.tests.cache.embeddedid;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class TestCompositeForeignKey {

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

    // this breaks if we uncomment the OneToMany relationships with cascade = ALL
    DB.createQuery(Concept.class).delete();
  }
}
