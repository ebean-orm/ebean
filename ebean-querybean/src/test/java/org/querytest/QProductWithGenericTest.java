package org.querytest;

import io.ebean.InTuples;

import org.example.domain.ProductWithGenericLong;
import org.example.domain.ProductWithGenericString;
import org.example.domain.query.QContact;
import org.example.domain.query.QProductWithGenericLong;
import org.example.domain.query.QProductWithGenericString;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class QProductWithGenericTest {

  @Test
  void findByLongId() {

    var entity = new ProductWithGenericLong();
    entity.setId(42L);
    entity.setName("Gadget");
    entity.save();

    var result = new QProductWithGenericLong()
      .id.eq(42L)
      .findOne();

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Gadget");
  }

  @Test
  void findByStringId() {

    var entity = new ProductWithGenericString();
    entity.setId("1234");
    entity.setName("Gadget");
    entity.save();

    var result = new QProductWithGenericString()
      .id.eq("1234")
      .findOne();

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Gadget");
  }
}
