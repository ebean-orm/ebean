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
    contact.workAddress(new Address("45 work", "workling", "wo"));
    contact.homeAddress(new Address("94 home st", "homeline", "wo"));

    contact.save();

    Contact found = new QContact()
      .id.eq(contact.id())
      .findOne();

    assert found != null;

    assertThat(found.workAddress().toString()).isEqualTo("Address[line1=45 work, line2=workling, city=wo]");
    assertThat(found.homeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

    Contact foundPartial = new QContact()
      .select(name, homeAddress)
      .id.eq(contact.id())
      .findOne();

    assert foundPartial != null;

    // invoke lazy loading on workAddress
    assertThat(foundPartial.workAddress().toString()).isEqualTo("Address[line1=45 work, line2=workling, city=wo]");
    assertThat(foundPartial.homeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

    Contact foundNoLazyLoading = new QContact()
      .select(name, homeAddress)
      .setDisableLazyLoading(true)
      .id.eq(contact.id())
      .findOne();

    assert foundNoLazyLoading != null;
    // no lazy loading on workAddress this time
    assertThat(foundNoLazyLoading.workAddress()).isNull();
    assertThat(foundNoLazyLoading.homeAddress().toString()).isEqualTo("Address[line1=94 home st, line2=homeline, city=wo]");

  }
}
