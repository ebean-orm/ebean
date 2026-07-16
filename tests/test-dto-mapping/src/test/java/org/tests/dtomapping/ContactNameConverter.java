package org.tests.dtomapping;

import org.tests.dtomapping.model.Contact;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Static converter for {@link CustomerTagsDto#getTags()} - collapses each {@link Contact} into a
 * plain {@code "First Last"} string. Mirrors a real-world case where a DTO's {@code List}
 * property has no registered nested DTO mapping of its own (e.g. it's populated from ad-hoc SQL,
 * or - as here - reduced to a simpler element type), so it's handled as a plain {@code SCALAR}
 * {@code @DtoConvert} rather than a {@code NESTED_MANY} delegate.
 */
public final class ContactNameConverter {

  private ContactNameConverter() {
  }

  public static List<String> toNames(List<Contact> contacts) {
    return contacts.stream()
      .map(c -> c.getFirstName() + " " + c.getLastName())
      .collect(Collectors.toList());
  }
}
