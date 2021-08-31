package org.example.records;

import org.example.records.query.QContact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.records.query.QContact.Alias.homeAddress;
import static org.example.records.query.QContact.Alias.name;

public class RecordAsEmbeddedTest {

  @Test
  void insert_query() {

    var contact = new Contact("Rob");
    contact.setWorkAddress(new Address("45 work", "workling", "wo"));
    contact.setHomeAddress(new Address("94 home st", "homeline", "wo"));

    contact.save();

    Contact found = new QContact()
      .id.eq(contact.getId())
      .findOne();

    assertThat(found.getWorkAddress().toString()).isEqualTo("Address[line1=45 work, line2=workling, city=wo]");
    assertThat(found.getHomeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

    Contact foundPartial = new QContact()
      .select(name, homeAddress)
      .id.eq(contact.getId())
      .findOne();

    // invoke lazy loading on getWorkAddress
    assertThat(foundPartial.getWorkAddress().toString()).isEqualTo("Address[line1=45 work, line2=workling, city=wo]");
    assertThat(foundPartial.getHomeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

    Contact foundNoLazyLoading = new QContact()
      .select(name, homeAddress)
      .setDisableLazyLoading(true)
      .id.eq(contact.getId())
      .findOne();

    // no lazy loading on getWorkAddress this time
    assertThat(foundNoLazyLoading.getWorkAddress()).isNull();
    assertThat(foundNoLazyLoading.getHomeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

  }
}
