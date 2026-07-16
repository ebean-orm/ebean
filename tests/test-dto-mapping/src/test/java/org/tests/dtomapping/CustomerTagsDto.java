package org.tests.dtomapping;

import io.ebean.annotation.DtoConvert;
import io.ebean.annotation.DtoPath;

import java.util.List;

/**
 * Plain DTO exercising a named variant excluding a non-nested ({@code SCALAR}) {@code List}
 * property - see docs/dto-mapping-requirements.md "Section G" follow-up: a {@code @DtoConvert}-
 * backed {@code List} property (element type has no registered {@code @DtoMapping} of its own,
 * e.g. populated from ad-hoc SQL in a real app) can still be excluded from a named variant, just
 * like a {@code NESTED_MANY} property - falling back to an empty list rather than {@code null}.
 */
public class CustomerTagsDto {

  private final Long id;
  private final String name;
  @DtoPath("contacts")
  @DtoConvert(value = ContactNameConverter.class, method = "toNames")
  private final List<String> tags;

  public CustomerTagsDto(Long id, String name, List<String> tags) {
    this.id = id;
    this.name = name;
    this.tags = tags;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getTags() {
    return tags;
  }
}
