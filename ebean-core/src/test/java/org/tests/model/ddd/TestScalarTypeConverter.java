package org.tests.model.ddd;

import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.ivo.Oid;
import org.tests.model.ivo.converter.AnEnumType;

import static org.assertj.core.api.Assertions.assertThat;

public class TestScalarTypeConverter {

  @Test
  public void integration() {

    DExhEntity exhEntity = new DExhEntity();
    exhEntity.setOid(new Oid<>(12));
    exhEntity.setAnEnumType(AnEnumType.ONE);

    Ebean.save(exhEntity);

    int count = Ebean.find(DExhEntity.class)
      .where().eq("anEnumType", AnEnumType.ONE)
      .findCount();

    assertThat(count).isGreaterThan(0);
  }
}
