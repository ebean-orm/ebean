package org.tests.dtomapping;

/**
 * Plain DTO with no framework attachment - nested ToOne target for {@link CustomerDto}.
 */
public class AddressDto {

  private final Long id;
  private final String line1;
  private final String city;

  public AddressDto(Long id, String line1, String city) {
    this.id = id;
    this.line1 = line1;
    this.city = city;
  }

  public Long getId() {
    return id;
  }

  public String getLine1() {
    return line1;
  }

  public String getCity() {
    return city;
  }
}
